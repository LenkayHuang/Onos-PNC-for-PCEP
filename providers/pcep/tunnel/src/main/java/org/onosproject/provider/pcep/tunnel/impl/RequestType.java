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
package org.onosproject.provider.pcep.tunnel.impl;

/**
 * Enum of request types between pcc and pcep.
 */
public enum RequestType {
    /**
     * Specifies the request type for PCC is to create new tunnel.
     */
    CREATE,

    /**
     * Specifies the request type for PCC is to update existing tunnel.
     */
    UPDATE,

    /**
     * Specifies the request type for PCC is to delete existing tunnel.
     */
    DELETE,

    /**
     * Specifies the request type for PCC to report existing tunnel.
     */
    LSP_STATE_RPT;
}