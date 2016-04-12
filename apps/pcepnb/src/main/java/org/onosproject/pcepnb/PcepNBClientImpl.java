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
package org.onosproject.pcepnb;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of PCEP NorthBound client.
 */
@Component(immediate = true)

public class PcepNBClientImpl {

    private static final Logger log = LoggerFactory.getLogger(PcepNBClientImpl.class);
    private final PcepNBClient client = new PcepNBClient();

    //private final Map<Integer, PcepNBClient> clientMap = new HashMap<>();//for multi-PNC architecture
    //private int PNCCount = 10;

    @Activate
    public void activate() {
        log.info("PNC-1.5:PcepNB start!");
        client.start();
    }

    @Deactivate
    public void deactivate() {
        //Close all connected clients
        //closeConnectedClients();
        log.info("PNC-1.5:PcepNB down!");
        client.stop();
    }

}
