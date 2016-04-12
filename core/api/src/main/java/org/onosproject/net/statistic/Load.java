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
package org.onosproject.net.statistic;

/**
 * Simple data repository for link load information.
 */
public interface Load {

    /**
     * Obtain the current observed rate (in bytes/s) on a link.
     *
     * @return long value
     */
    long rate();

    /**
     * Obtain the latest bytes counter viewed on that link.
     *
     * @return long value
     */
    long latest();

    /**
     * Indicates whether this load was built on valid values.
     *
     * @return boolean
     */
    boolean isValid();

    /**
     * Returns when this value was seen.
     *
     * @return epoch time
     */
    long time();

}
