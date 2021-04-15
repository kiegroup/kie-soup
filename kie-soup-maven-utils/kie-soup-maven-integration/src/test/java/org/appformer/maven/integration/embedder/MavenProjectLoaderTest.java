/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.appformer.maven.integration.embedder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.Aether;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MavenProjectLoaderTest {

    private static final String VERSION = "1.0-SNAPSHOT";
    private static final String GROUP_ID = "myGroupId";

    private static final String TRANSITIVE_ARTIFACT_ID = "transitiveDepArtifactId";
    private static final String DEP_ARTIFACT_ID = "depArtifactId";

    protected static final String PROJ_POM =
            "    <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "      xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "      <modelVersion>4.0.0</modelVersion>\n" +
                    "     \n" +
                    "      <groupId>myGroupId</groupId>\n" +
                    "      <artifactId>myproject</artifactId>\n" +
                    "      <version>1.0-SNAPSHOT</version>\n" +
                    "      <dependencies>" +
                    "          <dependency>" +
                    "             <groupId>" + GROUP_ID + "</groupId>\n" +
                    "              <artifactId>" + DEP_ARTIFACT_ID + "</artifactId>" +
                    "              <version>" + VERSION + "</version>" +
                    "          </dependency>" +
                    "      </dependencies>" +
                    "    </project>";
    
    static void installDependencies(boolean addTransitive) throws InstallationException {
        String repoRoot = MavenProjectLoaderTest.class.getResource("/").getFile();
        MavenSettings.reinitSettingsFromString(settings(repoRoot));

        String depJarURL = MavenProjectLoaderTest.class.getResource("/assets/depArtifactId").getFile();
        String depPomURL = MavenProjectLoaderTest.class.getResource("/assets/depArtifactId.pom").getFile();

        String depPomURLWithTransitive = MavenProjectLoaderTest.class.getResource("/assets/depArtifactId-transitive.pom").getFile();

        RepositorySystemSession session = Aether.getAether().getSession();
        RepositorySystem repo = Aether.getAether().getSystem();

        InstallRequest installRequest = new InstallRequest();

        installRequest.addArtifact(new DefaultArtifact(GROUP_ID,
                                                       TRANSITIVE_ARTIFACT_ID,
                                                       "",
                                                       "jar",
                                                       VERSION,
                                                       Collections.emptyMap(),
                                                       new File(depJarURL)));

        installRequest.addArtifact(new DefaultArtifact(GROUP_ID,
                                                       DEP_ARTIFACT_ID,
                                                       "",
                                                       "pom",
                                                       VERSION,
                                                       Collections.emptyMap(),
                                                       new File(addTransitive ? depPomURLWithTransitive : depPomURL)));

        installRequest.addArtifact(new DefaultArtifact(GROUP_ID,
                                                       DEP_ARTIFACT_ID,
                                                       "",
                                                       "jar",
                                                       VERSION,
                                                       Collections.emptyMap(),
                                                       new File(depJarURL)));

        repo.install(session, installRequest);
    }

    @Test
    public void shouldAddDependencyWithoutTransitiveTest() throws Exception {
        installDependencies(false);
        MavenProjectLoader.IS_FORCE_OFFLINE = true;
        ByteArrayInputStream targetPom = new ByteArrayInputStream(PROJ_POM.getBytes(StandardCharsets.UTF_8));
        MavenProject mavenProj = MavenProjectLoader.parseMavenPom(targetPom, true);
        assertEquals(1, mavenProj.getArtifacts().size());
        org.apache.maven.artifact.Artifact dependencyArtifact = mavenProj.getArtifacts().iterator().next();
        assertEquals(DEP_ARTIFACT_ID, dependencyArtifact.getArtifactId());
        assertEquals(GROUP_ID, dependencyArtifact.getGroupId());
        assertEquals(VERSION, dependencyArtifact.getVersion());
    }

    @Test
    public void shouldAddDependencyWithTransitiveTest() throws Exception {
        installDependencies(true);
        MavenProjectLoader.IS_FORCE_OFFLINE = true;
        MavenProjectLoader.IS_RESOLVE_TRANSITIVE = true;
        ByteArrayInputStream targetPom = new ByteArrayInputStream(PROJ_POM.getBytes(StandardCharsets.UTF_8));
        MavenProject mavenProj = MavenProjectLoader.parseMavenPom(targetPom, true);
        assertEquals(2, mavenProj.getArtifacts().size());

        org.apache.maven.artifact.Artifact dependencyArtifact = mavenProj.getArtifacts().stream()
                                                                         .filter(a -> a.getArtifactId().equals(DEP_ARTIFACT_ID))
                                                                         .findFirst().get();
        org.apache.maven.artifact.Artifact transitiveArtifact = mavenProj.getArtifacts().stream()
                                                                         .filter(a -> a.getArtifactId().equals(TRANSITIVE_ARTIFACT_ID))
                                                                         .findFirst().get();

        assertEquals(DEP_ARTIFACT_ID, dependencyArtifact.getArtifactId());
        assertEquals(GROUP_ID, dependencyArtifact.getGroupId());
        assertEquals(VERSION, dependencyArtifact.getVersion());

        assertEquals(TRANSITIVE_ARTIFACT_ID, transitiveArtifact.getArtifactId());
        assertEquals(GROUP_ID, transitiveArtifact.getGroupId());
        assertEquals(VERSION, transitiveArtifact.getVersion());
    }

    @Test
    public void shouldNotResolveTransitiveTest() throws Exception {
        installDependencies(true);
        MavenProjectLoader.IS_FORCE_OFFLINE = true;
        MavenProjectLoader.IS_RESOLVE_TRANSITIVE = false;
        ByteArrayInputStream targetPom = new ByteArrayInputStream(PROJ_POM.getBytes(StandardCharsets.UTF_8));
        MavenProject mavenProj = MavenProjectLoader.parseMavenPom(targetPom, true);
        assertEquals(1, mavenProj.getArtifacts().size());
    }

    static String settings(String localRepo) {
        return  "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "      xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n" +
                "                          http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "     <localRepository>" + localRepo + "</localRepository>" + 
                "</settings>";

    }

}
