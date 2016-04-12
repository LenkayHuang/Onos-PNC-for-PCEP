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
package org.onosproject.net.driver;

import static com.google.common.base.Preconditions.checkState;

/**
 * Base implementation of a driver behaviour.
 */
public class AbstractBehaviour implements Behaviour {

    private DriverData data;

    @Override
    public DriverData data() {
        return data;
    }

    @Override
    public void setData(DriverData data) {
        checkState(this.data == null, "Driver data already set");
        this.data = data;
    }
}
