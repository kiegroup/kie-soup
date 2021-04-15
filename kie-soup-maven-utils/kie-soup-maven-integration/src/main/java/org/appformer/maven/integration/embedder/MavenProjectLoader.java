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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.Aether;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenProjectLoader {

    public static final String FORCE_OFFLINE = "kie.maven.offline.force";
    public static final String RESOLVE_TRANSITVE = "kie.maven.offline.transitive.resolve";

    protected static boolean IS_FORCE_OFFLINE = Boolean.valueOf(System.getProperty(FORCE_OFFLINE, "false"));
    protected static boolean IS_RESOLVE_TRANSITIVE = Boolean.valueOf(System.getProperty(RESOLVE_TRANSITVE, "false"));

    private static final Logger log = LoggerFactory.getLogger(MavenProjectLoader.class);

    private static final String DUMMY_POM =
                    "    <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "      xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "      <modelVersion>4.0.0</modelVersion>\n" +
                    "     \n" +
                    "      <groupId>myGroupId</groupId>\n" +
                    "      <artifactId>myArtifactId</artifactId>\n" +
                    "      <version>1.0-SNAPSHOT</version>\n" +
                    "    </project>";

    static MavenProject mavenProject;

    public static MavenProject parseMavenPom(File pomFile) {
        return parseMavenPom(pomFile, false);
    }

    public static MavenProject parseMavenPom(File pomFile, boolean offline) {
        boolean hasPom = pomFile.exists();

        MavenRequest mavenRequest = createMavenRequest(offline);
        if (hasPom) {
            mavenRequest.setPom(pomFile.getAbsolutePath());
        }
        MavenEmbedder mavenEmbedder = null;
        try {
            mavenEmbedder = new MavenEmbedder(mavenRequest);
            return hasPom ?
                    mavenEmbedder.readProject(pomFile) :
                    mavenEmbedder.readProject(new ByteArrayInputStream(DUMMY_POM.getBytes(StandardCharsets.UTF_8)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (mavenEmbedder != null) {
                mavenEmbedder.dispose();
            }
        }
    }

    public static MavenProject parseMavenPom(InputStream pomStream) {
        return parseMavenPom(pomStream, false);
    }

    public static MavenProject parseMavenPom(InputStream pomStream, boolean offline) {
        MavenEmbedder mavenEmbedder = null;
        try {
            mavenEmbedder = newMavenEmbedder(offline);
            final MavenProject project = mavenEmbedder.readProject(pomStream);
            if (IS_FORCE_OFFLINE) {
                final Set<Artifact> artifacts = new HashSet<>();
                final RepositorySystemSession session = Aether.getAether().getSession();
                for (Dependency dep : project.getDependencies()) {
                    Artifact artifact = new DefaultArtifact(dep.getGroupId(),
                                                            dep.getArtifactId(),
                                                            dep.getVersion(),
                                                            dep.getScope(),
                                                            dep.getType(),
                                                            dep.getClassifier(),
                                                            new DefaultArtifactHandler());
                    if (resolve(session, artifact)) {
                        artifacts.add(artifact);
                        artifacts.addAll(collectTransitiveDependencies(session, dep));
                    } else {
                        log.error("Artifact can't be resolved {}'", artifact.toString());
                    }

                }
                if (!artifacts.isEmpty()) {
                    project.setArtifacts(artifacts);
                }
            }
            return project;
        } catch (Exception e) {
            log.error("Unable to create MavenProject from InputStream", e);
            throw new RuntimeException(e);
        } finally {
            if (mavenEmbedder != null) {
                mavenEmbedder.dispose();
            }
        }
    }

    private static boolean resolve(final RepositorySystemSession session,
                                   final Artifact artifact) {
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        final org.eclipse.aether.artifact.Artifact jarArtifact = toAetherArtifact(artifact);

        artifactRequest.setArtifact(jarArtifact);
        try {
            ArtifactResult result = Aether.getAether().getSystem().resolveArtifact(session,
                                                                                   artifactRequest);
            return result != null && result.isResolved();
        } catch (final Exception are) {
            log.info(are.getMessage(), are);
            return false;
        }
    }

    private static Set<Artifact> collectTransitiveDependencies(RepositorySystemSession session, Dependency dep) {
        if (IS_RESOLVE_TRANSITIVE) {
            try {
                org.eclipse.aether.graph.Dependency root = new org.eclipse.aether.graph.Dependency(toAetherArtifact(dep),
                                                                                                   dep.getScope());
                CollectRequest request = new CollectRequest(root, Collections.emptyList());
                CollectResult result = Aether.getAether().getSystem().collectDependencies(session, request);
                Set<Artifact> transitiveDeps = new HashSet<>();
                result.getRoot().accept(new DependencyVisitor() {

                    @Override
                    public boolean visitLeave(DependencyNode node) {
                        return false;
                    }

                    @Override
                    public boolean visitEnter(DependencyNode node) {
                        if (!result.getRoot().equals(node)) {
                            transitiveDeps.add(toArtifact(node.getDependency()));
                        }
                        return true;
                    }

                });

                return transitiveDeps.stream()
                                     .filter(tDep -> resolve(session, tDep))
                                     .collect(Collectors.toSet());

            } catch (DependencyCollectionException e) {
                log.warn("Not able to collect dependencies for {}", dep.toString());
                log.debug("Not able to collect dependencies.", e);
            }
        }
        return Collections.emptySet();
    }

    public static MavenEmbedder newMavenEmbedder(boolean offline) {
        MavenRequest mavenRequest = createMavenRequest(offline);
        MavenEmbedder mavenEmbedder;
        try {
            mavenEmbedder = new MavenEmbedder(mavenRequest);
        } catch (MavenEmbedderException e) {
            log.error("Unable to create new MavenEmbedder", e);
            throw new RuntimeException(e);
        }
        return mavenEmbedder;
    }

    public static MavenRequest createMavenRequest(boolean _offline) {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setLocalRepositoryPath(MavenSettings.getSettings().getLocalRepository());
        mavenRequest.setUserSettingsSource(MavenSettings.getUserSettingsSource());

        final boolean offline = IS_FORCE_OFFLINE || _offline;

        // BZ-1007894: If dependency is not resolvable and maven project builder does not complain about it,
        // then a <code>java.lang.NullPointerException</code> is thrown to the client.
        // So, the user will se an exception message "null", not descriptive about the real error.
        mavenRequest.setResolveDependencies(!offline);
        mavenRequest.setOffline(offline);
        return mavenRequest;
    }

    public static synchronized MavenProject loadMavenProject() {
        return loadMavenProject(false);
    }

    public static synchronized MavenProject loadMavenProject(boolean offline) {
        if (mavenProject == null) {
            File pomFile = new File("pom.xml");
            try {
                mavenProject = parseMavenPom(pomFile, offline);
            } catch (Exception e) {
                log.warn("Unable to parse pom.xml file of the running project: " + e.getMessage());
            }
        }
        return mavenProject;
    }

    public static boolean isOffline() {
        return IS_FORCE_OFFLINE;
    }

    private static Artifact toArtifact(org.eclipse.aether.graph.Dependency dependency) {
        return new DefaultArtifact(dependency.getArtifact().getGroupId(),
                                   dependency.getArtifact().getArtifactId(),
                                   dependency.getArtifact().getVersion(),
                                   dependency.getScope(),
                                   "jar",
                                   dependency.getArtifact().getClassifier(),
                                   new DefaultArtifactHandler());
    }

    private static org.eclipse.aether.artifact.DefaultArtifact toAetherArtifact(final Artifact artifact) {
        return aetherArtifact(artifact.getGroupId(),
                              artifact.getArtifactId(),
                              artifact.getClassifier(),
                              "jar",
                              artifact.getVersion());
    }

    private static org.eclipse.aether.artifact.DefaultArtifact toAetherArtifact(Dependency dep) {
        return aetherArtifact(dep.getGroupId(), dep.getArtifactId(),
                              dep.getClassifier(),
                              "jar",
                              dep.getVersion());
    }

    private static org.eclipse.aether.artifact.DefaultArtifact aetherArtifact(String groupId, String artifactId, String classifier, String ext, String version) {
        return new org.eclipse.aether.artifact.DefaultArtifact(groupId,
                                                               artifactId,
                                                               classifier,
                                                               ext,
                                                               version);
    }
}