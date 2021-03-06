/*
 * Copyright 2014 Open Networking Laboratory
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
package org.onosproject.net.resource.link;

import java.util.Collection;
import java.util.Set;

import org.onosproject.net.Link;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.resource.ResourceAllocation;
import org.onosproject.net.resource.ResourceRequest;

/**
 * Representation of allocated link resources.
 *
 * @deprecated 1.4.0 Emu Release
 */
@Deprecated
public interface LinkResourceAllocations extends ResourceAllocation {

    /**
     * Returns the {@link IntentId} associated with the request.
     *
     * @return the {@link IntentId} associated with the request
     */
    IntentId intentId();

    /**
     * Returns the set of target links.
     *
     * @return the set of target links
     */
    Collection<Link> links();

    /**
     * Returns the set of resource requests.
     *
     * @return the set of resource requests
     */
    Set<ResourceRequest> resources();

    /**
     * Returns allocated resource for the given link.
     *
     * @param link the target link
     * @return allocated resource for the link
     */
    Set<ResourceAllocation> getResourceAllocation(Link link);
}
