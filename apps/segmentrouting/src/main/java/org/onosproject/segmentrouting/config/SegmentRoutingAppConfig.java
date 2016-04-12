/*
 * Copyright 2016 Open Networking Laboratory
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

package org.onosproject.segmentrouting.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableSet;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.Config;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * App configuration object for Segment Routing.
 */
public class SegmentRoutingAppConfig extends Config<ApplicationId> {
    private static final String VROUTER_MACS = "vRouterMacs";
    private static final String VROUTER_ID = "vRouterId";
    private static final String SUPPRESS_SUBNET = "suppressSubnet";
    private static final String SUPPRESS_HOST = "suppressHost";

    @Override
    public boolean isValid() {
        return hasOnlyFields(VROUTER_MACS, VROUTER_ID, SUPPRESS_SUBNET, SUPPRESS_HOST) &&
                vRouterMacs() != null && vRouterId() != null &&
                suppressSubnet() != null && suppressHost() != null;
    }

    /**
     * Gets vRouters from the config.
     *
     * @return Set of vRouter MAC addresses, empty is not specified,
     *         or null if not valid
     */
    public Set<MacAddress> vRouterMacs() {
        if (!object.has(VROUTER_MACS)) {
            return ImmutableSet.of();
        }

        ImmutableSet.Builder<MacAddress> builder = ImmutableSet.builder();
        ArrayNode arrayNode = (ArrayNode) object.path(VROUTER_MACS);
        for (JsonNode jsonNode : arrayNode) {
            MacAddress mac;

            String macStr = jsonNode.asText(null);
            if (macStr == null) {
                return null;
            }
            try {
                mac = MacAddress.valueOf(macStr);
            } catch (IllegalArgumentException e) {
                return null;
            }

            builder.add(mac);
        }
        return builder.build();
    }

    /**
     * Sets vRouters to the config.
     *
     * @param vRouterMacs a set of vRouter MAC addresses
     * @return this {@link SegmentRoutingAppConfig}
     */
    public SegmentRoutingAppConfig setVRouterMacs(Set<MacAddress> vRouterMacs) {
        if (vRouterMacs == null) {
            object.remove(VROUTER_MACS);
        } else {
            ArrayNode arrayNode = mapper.createArrayNode();

            vRouterMacs.forEach(mac -> {
                arrayNode.add(mac.toString());
            });

            object.set(VROUTER_MACS, arrayNode);
        }
        return this;
    }

    /**
     * Gets vRouter device ID.
     *
     * @return Optional vRouter device ID,
     *         empty is not specified or null if not valid
     */
    public Optional<DeviceId> vRouterId() {
        if (!object.has(VROUTER_ID)) {
            return Optional.empty();
        }

        try {
            return Optional.of(DeviceId.deviceId(object.path(VROUTER_ID).asText()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Sets vRouter device ID.
     *
     * @param vRouterId vRouter device ID
     * @return this {@link SegmentRoutingAppConfig}
     */
    public SegmentRoutingAppConfig setVRouterId(DeviceId vRouterId) {
        if (vRouterId == null) {
            object.remove(VROUTER_ID);
        } else {
            object.put(VROUTER_ID, vRouterId.toString());
        }
        return this;
    }

    /**
     * Gets names of ports to which SegmentRouting does not push subnet rules.
     *
     * @return Set of port names, empty if not specified, or null
     *         if not valid
     */
    public Set<ConnectPoint> suppressSubnet() {
        if (!object.has(SUPPRESS_SUBNET)) {
            return ImmutableSet.of();
        }

        ImmutableSet.Builder<ConnectPoint> builder = ImmutableSet.builder();
        ArrayNode arrayNode = (ArrayNode) object.path(SUPPRESS_SUBNET);
        for (JsonNode jsonNode : arrayNode) {
            String portName = jsonNode.asText(null);
            if (portName == null) {
                return null;
            }
            try {
                builder.add(ConnectPoint.deviceConnectPoint(portName));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return builder.build();
    }

    /**
     * Sets names of ports to which SegmentRouting does not push subnet rules.
     *
     * @param suppressSubnet names of ports to which SegmentRouting does not push
     *                     subnet rules
     * @return this {@link SegmentRoutingAppConfig}
     */
    public SegmentRoutingAppConfig setSuppressSubnet(Set<ConnectPoint> suppressSubnet) {
        if (suppressSubnet == null) {
            object.remove(SUPPRESS_SUBNET);
        } else {
            ArrayNode arrayNode = mapper.createArrayNode();
            suppressSubnet.forEach(connectPoint -> {
                arrayNode.add(connectPoint.deviceId() + "/" + connectPoint.port());
            });
            object.set(SUPPRESS_SUBNET, arrayNode);
        }
        return this;
    }

    /**
     * Gets names of ports to which SegmentRouting does not push host rules.
     *
     * @return Set of port names, empty if not specified, or null
     *         if not valid
     */
    public Set<ConnectPoint> suppressHost() {
        if (!object.has(SUPPRESS_HOST)) {
            return ImmutableSet.of();
        }

        ImmutableSet.Builder<ConnectPoint> builder = ImmutableSet.builder();
        ArrayNode arrayNode = (ArrayNode) object.path(SUPPRESS_HOST);
        for (JsonNode jsonNode : arrayNode) {
            String portName = jsonNode.asText(null);
            if (portName == null) {
                return null;
            }
            try {
                builder.add(ConnectPoint.deviceConnectPoint(portName));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return builder.build();
    }

    /**
     * Sets names of ports to which SegmentRouting does not push host rules.
     *
     * @param suppressHost names of ports to which SegmentRouting does not push
     *                     host rules
     * @return this {@link SegmentRoutingAppConfig}
     */
    public SegmentRoutingAppConfig setSuppressHost(Set<ConnectPoint> suppressHost) {
        if (suppressHost == null) {
            object.remove(SUPPRESS_HOST);
        } else {
            ArrayNode arrayNode = mapper.createArrayNode();
            suppressHost.forEach(connectPoint -> {
                arrayNode.add(connectPoint.deviceId() + "/" + connectPoint.port());
            });
            object.set(SUPPRESS_HOST, arrayNode);
        }
        return this;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("vRouterMacs", vRouterMacs())
                .add("vRouterId", vRouterId())
                .add("suppressSubnet", suppressSubnet())
                .add("suppressHost", suppressHost())
                .toString();
    }
}
