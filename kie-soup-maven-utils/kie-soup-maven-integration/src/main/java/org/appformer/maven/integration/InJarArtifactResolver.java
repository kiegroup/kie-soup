/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.embedder.EmbeddedPomParser;
import org.appformer.maven.integration.embedder.MavenEmbedder;
import org.appformer.maven.integration.embedder.MavenProjectLoader;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.AFReleaseIdImpl;
import org.appformer.maven.support.DependencyFilter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class InJarArtifactResolver extends ArtifactResolver {

    private static final Logger log = LoggerFactory.getLogger(InJarArtifactResolver.class);

    private ClassLoader classLoader;
    private List<URL> jarRepository;
    private PomParser pomParser;

    InJarArtifactResolver(ClassLoader classLoader, AFReleaseId releaseId) {
        this.classLoader = classLoader;
        this.jarRepository = new ArrayList<>();
        init(releaseId);
    }

    public boolean isLoaded() {
        return pomParser != null;
    }
    // initialize in jar repository
    private void init(AFReleaseId releaseId) {
        jarRepository = buildResources();
        pomParser = buildPomParser(releaseId);
    }

    private List<URL> buildResources() {
        URL resourceURL = this.classLoader.getResource("");
        if (resourceURL == null) {
            return emptyList();
        }
        List<URL> resources = new ArrayList<>();
        try (InputStream is = resourceURL.openStream();
                ZipInputStream stream = new ZipInputStream(is);) {

            ZipEntry entry = null;
            while ((entry = stream.getNextEntry()) != null) {
                if (isInJarFolder(entry.getName())) {
                    resources.add(classLoader.getResource(entry.getName()));
                }
            }

            log.debug("Found in jar repository {}", resources);
        } catch (IOException e) {
            log.error("Error trying to open URL: {}", resourceURL);
        }
        return resources;
    }

    private boolean isInJarFolder(String name) {
        String[] paths = new String[]{"BOOT-INF/classes/KIE-INF/", "KIE-INF/lib/"};
        for (String path : paths) {
            if (name.startsWith(path) && name.endsWith(".jar")) {
                return true;
            }
        }
        return false;
    }

    private PomParser buildPomParser(AFReleaseId releaseId) {
        List<URL> url = jarRepository.stream().filter(e -> e.getFile().endsWith(toFile(releaseId))).collect(toList());
        if (url.isEmpty()) {
            return null;
        }
        URL pomFile = classLoader.getResource(url.get(0) + "!/META-INF/maven/" + releaseId.getGroupId() + "/" + releaseId.getArtifactId() + "/pom.xml");
        if (pomFile == null) {
            log.warn("Maven pom not found in path {}", pomFile);
            return null;
        }
        try (InputStream pomStream = pomFile.openStream()) {
            // dependencies were resolved already so there is no need to resolve them again
            MavenEmbedder mavenEmbedded = MavenProjectLoader.newMavenEmbedder(true);
            MavenProject mavenProject = mavenEmbedded.readProject(pomStream);
            PomParser artifactPomParser = new EmbeddedPomParser(mavenProject);
            return artifactPomParser;
        } catch (Exception e) {
            log.error("Could not read pom in jar {}", pomFile);
            return null;
        }

    }


    @Override
    public ArtifactLocation resolveArtifactLocation(AFReleaseId releaseId) {
        Optional<URL> url = tryInJar(releaseId);
        if (url.isPresent()) {
            DefaultArtifact artifact = new DefaultArtifact(releaseId.toExternalForm());
            return new ArtifactLocation(artifact.setFile(new File(url.get().toString())), url.get(), true);
        }
        return null;

    }

    @Override
    public Artifact resolveArtifact(AFReleaseId releaseId) {
        Optional<URL> url = tryInJar(releaseId);
        if (url.isPresent()) {
            log.info("Resolved in jar repository {}", url);
            DefaultArtifact artifact = new DefaultArtifact(releaseId.toExternalForm());
            return artifact.setFile(new File(url.get().toString()));
        }
        return null;
    }

    private Optional<URL> tryInJar(String artifactName) {
        for (URL inJarURL : jarRepository) {
            if (inJarURL.getFile().endsWith(artifactName)) {
                return Optional.of(inJarURL);
            }
        }
        return Optional.empty();
    }

    private Optional<URL> tryInJar(AFReleaseId releaseId) {
        return tryInJar(toFile(releaseId));
    }

    private String toFile(AFReleaseId releaseId) {
        return releaseId.getArtifactId() + "-" + releaseId.getVersion() + ".jar";
    }

    @Override
    public List<DependencyDescriptor> getArtifactDependecies(String artifactName) {
        AFReleaseId releaseId = new AFReleaseIdImpl(artifactName);
        PomParser pomParser = buildPomParser(releaseId);
        return pomParser != null ? pomParser.getPomDirectDependencies(DependencyFilter.COMPILE_FILTER) : emptyList();
    }

    @Override
    public List<DependencyDescriptor> getPomDirectDependencies(DependencyFilter dependencyFilter) {
        return (pomParser != null) ? pomParser.getPomDirectDependencies(dependencyFilter) : emptyList();
    }

}
