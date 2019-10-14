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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.Aether;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.junit.BeforeClass;
import org.junit.Test;

public class MavenProjectLoaderTest {
    
    private static final String VERSION = "1.0-SNAPSHOT";
    private static final String ARTIFACT_ID = "myArtifactId";
    private static final String GROUP_ID = "myGroupId";

    protected static final String PROJ =
            "    <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "      xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "      <modelVersion>4.0.0</modelVersion>\n" +
                    "     \n" +
                    "      <groupId>myGroupId</groupId>\n" +
                    "      <artifactId>myArtifactId2</artifactId>\n" +
                    "      <version>1.0-SNAPSHOT</version>\n" +
                    "      <dependencies>" +
                    "          <dependency>" +
                    "             <groupId>myGroupId</groupId>\n"   + 
                    "              <artifactId>myArtifactId</artifactId>" +
                    "              <version>1.0-SNAPSHOT</version>" +
                    "          </dependency>" +
                    "      </dependencies>" +                    
                    "    </project>";
    
    @BeforeClass
    public static void installDependency() throws InstallationException {
        String repoRoot = MavenProjectLoaderTest.class.getResource("/").getFile();
        MavenSettings.reinitSettingsFromString(settings(repoRoot));
        String jarURL = MavenProjectLoaderTest.class.getResource("/assets/myArtifactId").getFile();
        Artifact artifact = new DefaultArtifact(GROUP_ID, 
                                                ARTIFACT_ID, 
                                                "", 
                                                "jar", 
                                                VERSION,
                                                Collections.emptyMap(),
                                                new File(jarURL));
        InstallRequest request = new InstallRequest().addArtifact(artifact);
        RepositorySystemSession session = Aether.getAether().getSession();
        Aether.getAether().getSystem().install(session, request);
    }    
    
    @Test
    public void shouldAddDependencyArtifactsTest() throws Exception {
        MavenProjectLoader.IS_FORCE_OFFLINE = true;
        ByteArrayInputStream targetPom = new ByteArrayInputStream(PROJ.getBytes(StandardCharsets.UTF_8));
        MavenProject mavenProj = MavenProjectLoader.parseMavenPom(targetPom, true);
        assertEquals(1, mavenProj.getArtifacts().size());
        org.apache.maven.artifact.Artifact dependencyArtifact = mavenProj.getArtifacts().iterator().next();
        assertEquals(ARTIFACT_ID, dependencyArtifact.getArtifactId());
        assertEquals(GROUP_ID, dependencyArtifact.getGroupId());
        assertEquals(VERSION, dependencyArtifact.getVersion());
        
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
