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
package org.onosproject.iptopology.api;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

import org.onlab.packet.Ip4Address;

/**
 * Represents Pseudonode information of OSFP device.
 */
public class OspfPseudonode implements RouteIdentifier {
    private final RouterId designatedRouter;
    private final Ip4Address drInterface;
    private final ProtocolType type;

    /**
     * Constructor to initialize the values.
     *
     * @param designatedRouter Router Id of designated router
     * @param drInterface IP address of Designated Router interface
     * @param type Protocol ID
     */
    public OspfPseudonode(RouterId designatedRouter, Ip4Address drInterface, ProtocolType type) {
        this.designatedRouter = designatedRouter;
        this.drInterface = drInterface;
        this.type = type;
    }

    /**
     * Obtains designated Router Id.
     *
     * @return designated Router Id
     */
    public RouterId designatedRouter() {
        return designatedRouter;
    }

    /**
     * Obtains IP address of Designated Router interface.
     *
     * @return IP address of Designated Router interface
     */
    public Ip4Address drInterface() {
        return drInterface;
    }

    @Override
    public ProtocolType type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(designatedRouter, drInterface, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof OspfPseudonode) {
            OspfPseudonode other = (OspfPseudonode) obj;
            return Objects.equals(designatedRouter, other.designatedRouter)
                    && Objects.equals(drInterface, other.drInterface)
                    && Objects.equals(type, other.type);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("designatedRouter", designatedRouter)
                .add("drInterface", drInterface)
                .add("type", type)
                .toString();
    }
}