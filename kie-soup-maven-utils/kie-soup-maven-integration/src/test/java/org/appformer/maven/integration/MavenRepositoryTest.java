/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.appformer.maven.integration;

import java.io.File;
import java.util.Collection;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.appformer.maven.integration.Aether;
import org.appformer.maven.integration.MavenRepository;
import org.appformer.maven.integration.MavenRepositoryConfiguration;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MavenRepositoryTest {

    @Test
    public void testMirrors() {
        MavenRepositoryMock.setCustomSettingsFileName("settings_with_mirror.xml");
        final MavenRepository repo = new MavenRepositoryMock(Aether.getAether());
        final Collection<RemoteRepository> remoteRepos = repo.getRemoteRepositoriesForRequest();
        assertEquals(2, remoteRepos.size());
        for (final RemoteRepository remoteRepo : remoteRepos) {
            assertTrue(remoteRepo.getId().equals("qa") ||
                       remoteRepo.getId().equals("foo"));
        }
    }

    @Test
    public void testProxy() {
        MavenRepositoryMock.setCustomSettingsFileName("settings_custom.xml");
        final MavenRepository repo = new MavenRepositoryMock(Aether.getAether());
        final Collection<RemoteRepository> remoteRepos = repo.getRemoteRepositoriesForRequest();
        assertEquals(5, remoteRepos.size());
        for (RemoteRepository remoteRepository : remoteRepos) {
            if (remoteRepository.getId().equals("test-server")) {
                assertNotNull(remoteRepository.getProxy());
            }
        }
    }

    public static class MavenRepositoryMock extends MavenRepository {

        private static String customSettingsFileName;

        // This is needed to do like this, because the repository is initialized by calling parent constructor with super().
        public static void setCustomSettingsFileName(final String customSettingsFileNameParam) {
            customSettingsFileName = customSettingsFileNameParam;
        }

        protected MavenRepositoryMock(final Aether aether) {
            super(aether);
        }

        @Override
        protected MavenRepositoryConfiguration getMavenRepositoryConfiguration() {
            return new MavenRepositoryConfiguration(getMavenSettings());
        }

        private Settings getMavenSettings() {
            final String path = getClass().getResource(".").toString().substring("file:".length());
            final File testSettingsFile = new File(path + customSettingsFileName);
            assertTrue(testSettingsFile.exists());

            final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
            final DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile( testSettingsFile );

            try {
                return settingsBuilder.build( request ).getEffectiveSettings();
            } catch ( final SettingsBuildingException e ) {
                throw new RuntimeException(e);
            }
        }
    }
}
