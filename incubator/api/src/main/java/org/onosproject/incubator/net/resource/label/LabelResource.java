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
package org.onosproject.incubator.net.resource.label;

import com.google.common.annotations.Beta;
import org.onosproject.net.Annotated;
import org.onosproject.net.DeviceId;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.Provided;

/**
 * Representation of label resource.
 */
@Beta
public interface LabelResource extends Annotated, Provided, NetworkResource {
    /**
     * Returns device id.
     * @return DeviceId
     */
    DeviceId deviceId();

    /**
     * Returns label resource identifier.
     *
     * @return resource id
     */
    LabelResourceId labelResourceId();
}
