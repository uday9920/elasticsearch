/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.bootstrap;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.monitor.os.OsProbe;
import org.elasticsearch.monitor.process.ProcessProbe;
import org.elasticsearch.test.ESTestCase;

public class BootstrapSettingsTests extends ESTestCase {

    public void testDefaultSettings() {
        assertTrue(BootstrapSettings.SECURITY_FILTER_BAD_DEFAULTS_SETTING.get(Settings.EMPTY));
        assertFalse(BootstrapSettings.MLOCKALL_SETTING.get(Settings.EMPTY));
        assertTrue(BootstrapSettings.SECCOMP_SETTING.get(Settings.EMPTY));
        assertTrue(BootstrapSettings.CTRLHANDLER_SETTING.get(Settings.EMPTY));
    }

    public void testEnforceMaxFileDescriptorLimits() {
        // nothing should happen since we are in OOB mode
        Bootstrap.enforceOrLogLimits(Settings.EMPTY);

        Settings build = Settings.builder().put(randomFrom(Bootstrap.ENFORCE_SETTINGS.toArray(new Setting[0])).getKey(),
            "127.0.0.1").build();
        long maxFileDescriptorCount = ProcessProbe.getInstance().getMaxFileDescriptorCount();
        try {
            Bootstrap.enforceOrLogLimits(build);
            if (maxFileDescriptorCount != -1 && maxFileDescriptorCount < (1 << 16)) {
                fail("must have enforced limits: " + maxFileDescriptorCount);
            }
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("max file descriptors"));
        }
    }

}