/*
 * Copyright 2014-2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.intent.impl.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.util.Frequency;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ChannelSpacing;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultOchSignalComparator;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.OchPort;
import org.onosproject.net.OchSignal;
import org.onosproject.net.OchSignalType;
import org.onosproject.net.Path;
import org.onosproject.net.Port;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.OpticalConnectivityIntent;
import org.onosproject.net.intent.OpticalPathIntent;
import org.onosproject.net.intent.impl.IntentCompilationException;
import org.onosproject.net.newresource.ResourceAllocation;
import org.onosproject.net.newresource.Resource;
import org.onosproject.net.newresource.ResourceService;
import org.onosproject.net.newresource.Resources;
import org.onosproject.net.resource.link.LinkResourceAllocations;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An intent compiler for {@link org.onosproject.net.intent.OpticalConnectivityIntent}.
 */
@Component(immediate = true)
public class OpticalConnectivityIntentCompiler implements IntentCompiler<OpticalConnectivityIntent> {

    protected static final Logger log = LoggerFactory.getLogger(OpticalConnectivityIntentCompiler.class);
    // By default, allocate 50 GHz lambdas (4 slots of 12.5 GHz) for each intent.
    private static final int SLOT_COUNT = 4;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ResourceService resourceService;

    @Activate
    public void activate() {
        intentManager.registerCompiler(OpticalConnectivityIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterCompiler(OpticalConnectivityIntent.class);
    }

    @Override
    public List<Intent> compile(OpticalConnectivityIntent intent,
                                List<Intent> installable,
                                Set<LinkResourceAllocations> resources) {
        // Check if source and destination are optical OCh ports
        ConnectPoint src = intent.getSrc();
        ConnectPoint dst = intent.getDst();
        Port srcPort = deviceService.getPort(src.deviceId(), src.port());
        Port dstPort = deviceService.getPort(dst.deviceId(), dst.port());
        checkArgument(srcPort instanceof OchPort);
        checkArgument(dstPort instanceof OchPort);

        log.debug("Compiling optical connectivity intent between {} and {}", src, dst);

        // Release of intent resources here is only a temporary solution for handling the
        // case of recompiling due to intent restoration (when intent state is FAILED).
        // TODO: try to release intent resources in IntentManager.
        resourceService.release(intent.id());

        // Reserve OCh ports
        Resource srcPortResource = Resources.discrete(src.deviceId(), src.port()).resource();
        Resource dstPortResource = Resources.discrete(dst.deviceId(), dst.port()).resource();
        List<ResourceAllocation> allocation = resourceService.allocate(intent.id(), srcPortResource, dstPortResource);
        if (allocation.isEmpty()) {
            throw new IntentCompilationException("Unable to reserve ports for intent " + intent);
        }

        // Calculate available light paths
        Set<Path> paths = getOpticalPaths(intent);

        // Static or dynamic lambda allocation
        String staticLambda = srcPort.annotations().value(AnnotationKeys.STATIC_LAMBDA);
        OchPort srcOchPort = (OchPort) srcPort;
        OchPort dstOchPort = (OchPort) dstPort;
        OchSignal ochSignal;

        // Use first path that can be successfully reserved
        for (Path path : paths) {

            // FIXME: need to actually reserve the lambda for static lambda's
            if (staticLambda != null) {
                ochSignal = new OchSignal(Frequency.ofHz(Long.parseLong(staticLambda)),
                        srcOchPort.lambda().channelSpacing(),
                        srcOchPort.lambda().slotGranularity());
            } else if (!srcOchPort.isTunable() || !dstOchPort.isTunable()) {
                // FIXME: also check destination OCh port
                ochSignal = srcOchPort.lambda();
            } else {
                // Request and reserve lambda on path
                List<OchSignal> lambdas = assignWavelength(intent, path);
                if (lambdas.isEmpty()) {
                    continue;
                }
                ochSignal = OchSignal.toFixedGrid(lambdas, ChannelSpacing.CHL_50GHZ);
            }

            // Create installable optical path intent
            // Only support fixed grid for now
            OchSignalType signalType = OchSignalType.FIXED_GRID;

            Intent newIntent = OpticalPathIntent.builder()
                    .appId(intent.appId())
                    .src(intent.getSrc())
                    .dst(intent.getDst())
                    .path(path)
                    .lambda(ochSignal)
                    .signalType(signalType)
                    .bidirectional(intent.isBidirectional())
                    .build();

            return ImmutableList.of(newIntent);
        }

        // Release port allocations if unsuccessful
        resourceService.release(intent.id());

        throw new IntentCompilationException("Unable to find suitable lightpath for intent " + intent);
    }

    /**
     * Request and reserve first available wavelength across path.
     *
     * @param path path in WDM topology
     * @return first available lambda allocated
     */
    private List<OchSignal> assignWavelength(Intent intent, Path path) {
        Set<OchSignal> lambdas = findCommonLambdasOverLinks(path.links());
        if (lambdas.isEmpty()) {
            return Collections.emptyList();
        }

        List<OchSignal> minLambda = findFirstLambda(lambdas, slotCount());
        List<Resource> lambdaResources = path.links().stream()
                .flatMap(x -> Stream.of(
                        Resources.discrete(x.src().deviceId(), x.src().port()).resource(),
                        Resources.discrete(x.dst().deviceId(), x.dst().port()).resource()
                ))
                .flatMap(x -> minLambda.stream().map(l -> x.child(l)))
                .collect(Collectors.toList());

        List<ResourceAllocation> allocations = resourceService.allocate(intent.id(), lambdaResources);
        if (allocations.isEmpty()) {
            log.info("Resource allocation for {} failed (resource request: {})", intent, lambdaResources);
            return Collections.emptyList();
        }

        return minLambda;
    }

    /**
     * Get the number of 12.5 GHz slots required for the path.
     *
     * For now this returns a constant value of 4 (i.e., fixed grid 50 GHz slot),
     * but in the future can depend on optical reach, line rate, transponder port capabilities, etc.
     *
     * @return number of slots
     */
    private int slotCount() {
        return SLOT_COUNT;
    }

    private Set<OchSignal> findCommonLambdasOverLinks(List<Link> links) {
        return links.stream()
                .flatMap(x -> Stream.of(
                        Resources.discrete(x.src().deviceId(), x.src().port()).id(),
                        Resources.discrete(x.dst().deviceId(), x.dst().port()).id()
                ))
                .map(x -> resourceService.getAvailableResourceValues(x, OchSignal.class))
                .map(x -> (Set<OchSignal>) ImmutableSet.copyOf(x))
                .reduce(Sets::intersection)
                .orElse(Collections.emptySet());
    }

    /**
     * Returns list of consecutive resources in given set of lambdas.
     *
     * @param lambdas list of lambdas
     * @param count number of consecutive lambdas to return
     * @return list of consecutive lambdas
     */
    private List<OchSignal> findFirstLambda(Set<OchSignal> lambdas, int count) {
        // Sort available lambdas
        List<OchSignal> lambdaList = new ArrayList<>(lambdas);
        lambdaList.sort(new DefaultOchSignalComparator());

        // Look ahead by count and ensure spacing multiplier is as expected (i.e., no gaps)
        for (int i = 0; i < lambdaList.size() - count; i++) {
            if (lambdaList.get(i).spacingMultiplier() + 2 * count ==
                    lambdaList.get(i + count).spacingMultiplier()) {
                return lambdaList.subList(i, i + count);
            }
        }

        return Collections.emptyList();
    }

    private ConnectPoint staticPort(ConnectPoint connectPoint) {
        Port port = deviceService.getPort(connectPoint.deviceId(), connectPoint.port());

        String staticPort = port.annotations().value(AnnotationKeys.STATIC_PORT);

        // FIXME: need a better way to match the port
        if (staticPort != null) {
            for (Port p : deviceService.getPorts(connectPoint.deviceId())) {
                if (staticPort.equals(p.number().name())) {
                    return new ConnectPoint(p.element().id(), p.number());
                }
            }
        }

        return null;
    }

    /**
     * Calculates optical paths in WDM topology.
     *
     * @param intent optical connectivity intent
     * @return set of paths in WDM topology
     */
    private Set<Path> getOpticalPaths(OpticalConnectivityIntent intent) {
        // Route in WDM topology
        Topology topology = topologyService.currentTopology();
        LinkWeight weight = edge -> {
            // Disregard inactive or non-optical links
            if (edge.link().state() == Link.State.INACTIVE) {
                return -1;
            }
            if (edge.link().type() != Link.Type.OPTICAL) {
                return -1;
            }
            // Adhere to static port mappings
            DeviceId srcDeviceId = edge.link().src().deviceId();
            if (srcDeviceId.equals(intent.getSrc().deviceId())) {
                ConnectPoint srcStaticPort = staticPort(intent.getSrc());
                if (srcStaticPort != null) {
                    return srcStaticPort.equals(edge.link().src()) ? 1 : -1;
                }
            }
            DeviceId dstDeviceId = edge.link().dst().deviceId();
            if (dstDeviceId.equals(intent.getDst().deviceId())) {
                ConnectPoint dstStaticPort = staticPort(intent.getDst());
                if (dstStaticPort != null) {
                    return dstStaticPort.equals(edge.link().dst()) ? 1 : -1;
                }
            }

            return 1;
        };

        ConnectPoint start = intent.getSrc();
        ConnectPoint end = intent.getDst();
        Set<Path> paths = topologyService.getPaths(topology, start.deviceId(),
                end.deviceId(), weight);

        return paths;
    }
}
