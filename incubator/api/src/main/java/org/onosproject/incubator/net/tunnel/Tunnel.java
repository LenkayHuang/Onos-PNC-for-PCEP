/*
 * Copyright 2015 Open Networking Laboratory
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
package org.onosproject.incubator.net.tunnel;

import com.google.common.annotations.Beta;
import org.onosproject.core.DefaultGroupId;
import org.onosproject.net.Annotated;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.Path;
import org.onosproject.net.Provided;

/**
 * Abstraction of a generalized Tunnel entity (bandwidth pipe) for either L3/L2
 * networks or L1/L0 networks, representation of e.g., VLAN, GRE tunnel, MPLS
 * LSP, L1 ODUk connection, WDM OCH, etc.. Each Tunnel is associated with at
 * least two tunnel end point objects that model the logical ports essentially.
 * Note that it supports nested case.
 */
@Beta
public interface Tunnel extends Annotated, Provided, NetworkResource {

    /**
     * Tunnel technology type.
     */
    enum Type {
        /**
         * Signifies that this is a MPLS tunnel.
         */
        MPLS,
        /**
         * Signifies that this is a L2 tunnel.
         */
        VLAN,
        /**
         * Signifies that this is a DC L2 extension tunnel.
         */
        VXLAN,
        /**
         * Signifies that this is a L3 tunnel.
         */
        GRE,
        /**
         * Signifies that this is a L1 OTN tunnel.
         */
        ODUK,
        /**
         * Signifies that this is a L0 OCH tunnel.
         */
        OCH
    }

    /**
     * Representation of the tunnel state.
     */
    public enum State {
        /**
         * Signifies that a tunnel is currently in a initialized state.
         */
        INIT,
        /**
         * Signifies that a tunnel is currently established but no traffic.
         */
        ESTABLISHED,
        /**
         * Signifies that a tunnel is currently active. This state means that
         * this tunnel is available. It can be borrowed by consumer.
         */
        ACTIVE,
        /**
         * Signifies that a tunnel is currently out of service.
         */
        FAILED,
        /**
         * Signifies that a tunnel is currently inactive. This state means that
         * this tunnel can not be borrowed by consumer.
         */
        INACTIVE
    }

    /**
     * Returns the tunnel state.
     *
     * @return tunnel state
     */
    State state();

    /**
     * the origin of a tunnel.
     *
     * @return the origin of a tunnel
     */
    TunnelEndPoint src();

    /**
     * the terminal of a tunnel.
     *
     * @return the terminal of a tunnel
     */
    TunnelEndPoint dst();

    /**
     * Returns the tunnel type.
     *
     * @return tunnel type
     */
    Type type();

    /**
     * Returns group flow table id which a tunnel match up.
     *
     * @return OpenFlowGroupId
     */
    DefaultGroupId groupId();

    /**
     * Returns tunnel identify generated by ONOS as primary key.
     *
     * @return TunnelId
     */
    TunnelId tunnelId();

    /**
     * Return the name of a tunnel.
     *
     * @return Tunnel Name
     */
    TunnelName tunnelName();

    /**
     * Network resource backing the tunnel, e.g. lambda, VLAN id, MPLS tag.
     *
     * @return backing resource
     */
    NetworkResource resource();

    /**
     * Returns the path of the tunnel.
     *
     * @return the path of the tunnel
     */
    Path path();
}
