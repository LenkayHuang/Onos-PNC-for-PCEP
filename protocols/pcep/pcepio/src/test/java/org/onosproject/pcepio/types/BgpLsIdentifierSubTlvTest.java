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
package org.onosproject.pcepio.types;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/**
 * Test of the BgpLsIdentifierSubTlv.
 */
public class BgpLsIdentifierSubTlvTest {
    private final BgpLsIdentifierSubTlv tlv1 = BgpLsIdentifierSubTlv.of(1);
    private final BgpLsIdentifierSubTlv sameAsTlv1 = BgpLsIdentifierSubTlv.of(1);
    private final BgpLsIdentifierSubTlv tlv2 = BgpLsIdentifierSubTlv.of(2);

    @Test
    public void basics() {
        new EqualsTester()
        .addEqualityGroup(tlv1, sameAsTlv1)
        .addEqualityGroup(tlv2)
        .testEquals();
    }
}
