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

package org.appformer.maven.integration.embedder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsSource;
import org.appformer.maven.integration.MavenRepositoryConfiguration;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.appformer.maven.integration.embedder.MavenSettings.CUSTOM_SETTINGS_PROPERTY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenEmbedderTest {

    private final String EMPTY_SETTINGS = "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n" +
                                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                          "      xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n" +
                                          "                          http://maven.apache.org/xsd/settings-1.0.0.xsd\"/>\n";

    final ComponentProvider componentProviderMocked = mock(ComponentProvider.class);

    @Test
    public void testInvalidLocalDependency() throws MavenEmbedderException, ProjectBuildingException, ComponentLookupException, SettingsBuildingException, InvalidRepositoryException {
        final MavenExecutionRequestPopulator mavenExecutionRequestPopulator =  mock(MavenExecutionRequestPopulator.class);
        final Logger logger =  mock(Logger.class);
        final RepositorySystem repositorySystem = mock(RepositorySystem.class);
        final ArtifactRepository artifactRepository = mock(ArtifactRepository.class);

        final SettingsBuilder settingsBuilder =  mock(SettingsBuilder.class);
        final SettingsBuildingResult settingsBuildingResult = mock(SettingsBuildingResult.class);
        final ProjectBuilder projectBuilderMock =  mock(ProjectBuilder.class);
        final DependencyResolutionResult drr = mock(DependencyResolutionResult.class);
        final ProjectBuildingResult projectBuildingResult =  mock(ProjectBuildingResult.class);
        final ArtifactDescriptorException exception = mock(ArtifactDescriptorException.class);
        final ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();

        final ArtifactDescriptorResult result = new ArtifactDescriptorResult(request);
        final Artifact artifactResult = mock(Artifact .class);

        final List<Exception> list = singletonList(exception);

        request.setArtifact(artifactResult);
        result.setArtifact(artifactResult);

        doReturn(settingsBuilder).when(componentProviderMocked).lookup(SettingsBuilder.class);
        doReturn(settingsBuildingResult).when(settingsBuilder).build(any(SettingsBuildingRequest.class));
        when(settingsBuildingResult.getEffectiveSettings()).thenReturn(mock(Settings.class));
        when(componentProviderMocked.getSystemClassLoader()).thenReturn(getClass().getClassLoader());


        doReturn(artifactRepository).when(repositorySystem).createLocalRepository(any(File.class));

        doReturn(new File("").toPath().toString()).when(artifactRepository).getBasedir();

        doReturn(repositorySystem).when(componentProviderMocked).lookup(RepositorySystem.class);

        doReturn(projectBuilderMock).when(componentProviderMocked).lookup(ProjectBuilder.class);

        doReturn(logger).when(componentProviderMocked).lookup(Logger.class);
        doReturn(mavenExecutionRequestPopulator).when(componentProviderMocked).lookup(MavenExecutionRequestPopulator.class);

        doReturn(projectBuildingResult).when(projectBuilderMock).build(any(ModelSource.class), any(ProjectBuildingRequest.class));
        when(projectBuildingResult.getDependencyResolutionResult()).thenReturn(drr);
        when(projectBuildingResult.getDependencyResolutionResult()).thenReturn(drr);
        when(drr.getCollectionErrors()).thenReturn(list);
        when(exception.getResult()).thenReturn(result);

        boolean[] didExecuteTryRemoveLocalArtifact = {false};
        final MavenRequest mavenRequest = createMavenRequest(null);
        final MavenEmbedder embedder = new MavenEmbedderMock2( mavenRequest, null ) {
            void tryRemoveLocalArtifact(Artifact artifact) {
                didExecuteTryRemoveLocalArtifact[0] = true;
                assertEquals(artifact, artifactResult);
            }
        };

        assertThatThrownBy(() -> embedder.readProject(mock(InputStream.class)))
                .isInstanceOf(MavenEmbedderException.class);

        assertTrue(didExecuteTryRemoveLocalArtifact[0]);
    }


    @Test
    public void testExternalRepositories() {
        String oldSettingsXmlPath = System.getProperty( CUSTOM_SETTINGS_PROPERTY );
        try {
            if (oldSettingsXmlPath != null) {
                System.clearProperty( CUSTOM_SETTINGS_PROPERTY );
            }
            MavenSettings.reinitSettingsFromString( EMPTY_SETTINGS );

            final MavenRequest mavenRequest = createMavenRequest( null );
            final MavenEmbedder embedder = new MavenEmbedderWithRepoMock( mavenRequest );
            final MavenExecutionRequest request = embedder.getMavenExecutionRequest();

            assertNotNull( request );

            final List<ArtifactRepository> remoteRepositories = request.getRemoteRepositories();
            assertEquals( 2,
                          remoteRepositories.size() );
            for ( ArtifactRepository remoteRepository : remoteRepositories ) {
                assertTrue( remoteRepository.getId().equals( "central" )
                                    || remoteRepository.getId().equals( "kie-wb-m2-repo" ) );
            }

        } catch ( MavenEmbedderException mee ) {
            fail( mee.getMessage() );
        } finally {
            if (oldSettingsXmlPath != null) {
                System.setProperty( CUSTOM_SETTINGS_PROPERTY, oldSettingsXmlPath );
            }
            MavenSettings.reinitSettings();
        }
    }

    @Test
    public void testCustomSettingSource() {
        try {
            final MavenRequest mavenRequest = createMavenRequest(new SettingsSourceMock( "<settings/>" ) );
            final MavenEmbedder embedder = new MavenEmbedderWithRepoMock( mavenRequest );
            final MavenExecutionRequest request = embedder.getMavenExecutionRequest();

            assertNotNull( request );

            Assert.assertEquals( "<settings/>", readFileAsString( request.getUserSettingsFile() ).trim() );

        } catch ( MavenEmbedderException mee ) {
            fail( mee.getMessage() );
        }
    }

    @Test
    public void testProxies() {
        String oldSettingsXmlPath = System.getProperty( CUSTOM_SETTINGS_PROPERTY );
        try {
            if (oldSettingsXmlPath != null) {
                System.clearProperty( CUSTOM_SETTINGS_PROPERTY );
            }
            MavenSettings.reinitSettingsFromString(EMPTY_SETTINGS);

            final MavenRequest mavenRequest = createMavenRequest(null);
            final MavenEmbedder embedder = new MavenEmbedderWithProxyMock( mavenRequest );
            final MavenExecutionRequest request = embedder.getMavenExecutionRequest();

            assertNotNull( request );

            final List<Proxy> proxies = request.getProxies();
            assertEquals( 1, proxies.size() );
            assertEquals( "MyProxy", proxies.get(0).getId() );

        } catch ( MavenEmbedderException mee ) {
            fail( mee.getMessage() );
        } finally {
            if (oldSettingsXmlPath != null) {
                System.setProperty( CUSTOM_SETTINGS_PROPERTY, oldSettingsXmlPath );
            }
            MavenSettings.reinitSettings();
        }
    }

    class MavenEmbedderMock2 extends MavenEmbedder {

        protected MavenEmbedderMock2(MavenRequest mavenRequest, ComponentProvider componentProvider) throws MavenEmbedderException {
            super(mavenRequest, componentProviderMocked);
        }

        public String getLocalRepositoryPath(){
            return new File("").toString();
        }

        void init(){
        }

        ProjectBuildingRequest getProjectBuildingRequest() throws ComponentLookupException {
            return mock(ProjectBuildingRequest.class);
        }
    }

    public static abstract class MavenEmbedderMock extends MavenEmbedder {

        public MavenEmbedderMock( MavenRequest mavenRequest ) throws MavenEmbedderException {
            super( mavenRequest );
        }

        @Override
        protected MavenRepositoryConfiguration getMavenRepositoryConfiguration() {
            return new MavenRepositoryConfiguration(getMavenSettings());
        }

        private Settings getMavenSettings() {
            String path = getClass().getResource( "." ).toString().substring( "file:".length() );
            File testSettingsFile = new File( path + getSettingsFile() );
            assertTrue( testSettingsFile.exists() );

            SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
            DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile( testSettingsFile );

            try {
                return settingsBuilder.build( request ).getEffectiveSettings();
            } catch ( SettingsBuildingException e ) {
                throw new RuntimeException( e );
            }
        }

        protected abstract String getSettingsFile();
    }

    public static class MavenEmbedderWithRepoMock extends MavenEmbedderMock {

        public MavenEmbedderWithRepoMock( MavenRequest mavenRequest ) throws MavenEmbedderException {
            super( mavenRequest );
        }

        @Override
        protected String getSettingsFile() {
            return "settings_with_repositories.xml";
        }
    }

    public static class MavenEmbedderWithProxyMock extends MavenEmbedderMock {

        public MavenEmbedderWithProxyMock( MavenRequest mavenRequest ) throws MavenEmbedderException {
            super( mavenRequest );
        }

        @Override
        protected String getSettingsFile() {
            return "settings_with_proxies.xml";
        }
    }

    private static MavenRequest createMavenRequest(SettingsSource settingsSource) {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setLocalRepositoryPath( MavenSettings.getSettings().getLocalRepository() );
        mavenRequest.setUserSettingsSource(settingsSource != null ? settingsSource : MavenSettings.getUserSettingsSource());
        mavenRequest.setResolveDependencies( true );
        mavenRequest.setOffline( true );
        return mavenRequest;
    }

    public static class SettingsSourceMock implements SettingsSource {

        private final String settings;

        public SettingsSourceMock( String settings ) {
            this.settings = settings;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream( settings.getBytes( "UTF-8" ) );
        }

        @Override
        public String getLocation() {
            return "test";
        }
    }

    private static String readFileAsString(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader( new FileInputStream( file), Charset.forName( "UTF-8" )));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { }
            }
        }
        return sb.toString();
    }

}
