/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.AFReleaseIdImpl;
import org.appformer.maven.support.DependencyFilter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public abstract class AbstractFilesArtifactResolver extends ArtifactResolver {

    private static final Logger log = LoggerFactory.getLogger(AbstractFilesArtifactResolver.class);

    private ClassLoader classLoader;
    private List<URL> jarRepository;
    private List<URL> effectivePoms;
    private PomParser pomParser;


    AbstractFilesArtifactResolver(ClassLoader classLoader, AFReleaseId releaseId) {
        this.classLoader = classLoader;
        this.jarRepository = new ArrayList<>();
        this.effectivePoms = new ArrayList<>();
        init(releaseId);
    }

    public boolean isLoaded() {
        return pomParser != null;
    }

    // initialize in jar repository
    private void init(AFReleaseId releaseId) {
        jarRepository = buildResources(name -> isInJarStructuredFolder(name, "jar"));
        effectivePoms = buildResources(name -> isInJarStructuredFolder(name, "pom"));
        pomParser = buildPomParser(releaseId);
    }

    protected abstract List<URL> buildResources(Predicate<String> predicate);

    protected abstract URL getURL(String path);

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<URL> getJarRepository() {
        return jarRepository;
    }

    public PomParser getPomParser() {
        return pomParser;
    }

    public List<URL> getEffectivePoms() {
        return effectivePoms;
    }

    private boolean isInJarStructuredFolder(String name, String type) {
        String[] paths = new String[]{"BOOT-INF/classes/KIE-INF/", "KIE-INF/lib/"};
        for (String path : paths) {
            // a bit of a hack "contains call"
            if (name.contains(path) && name.endsWith("." + type)) {
                return true;
            }
        }
        return false;
    }

    private PomParser buildPomParser(AFReleaseId releaseId) {
        List<URL> url = effectivePoms.stream().filter(e -> e.getFile().endsWith(toFile(releaseId, "pom"))).collect(toList());
        if (url.isEmpty()) {
            return null;
        }
        String path = url.get(0).toExternalForm();
        URL pomFile = getURL(path);
        if (pomFile == null) {
            log.warn("Maven pom not found in path {}", path);
            return null;
        }
        try (InputStream pomStream = pomFile.openStream()) {
            DefaultModelReader reader = new DefaultModelReader();
            Model model = reader.read(pomStream, Collections.emptyMap());
            // dependencies were resolved already creating the effective pom during kjar creation
            return filter -> {
                    List<DependencyDescriptor> deps = new ArrayList<>();
                    for (Dependency dep : model.getDependencies()) {
                        DependencyDescriptor depDescr = new DependencyDescriptor(dep);
                        if (depDescr.isValid() && filter.accept(depDescr.getReleaseId(), depDescr.getScope())) {
                            deps.add(depDescr);
                        }
                    }
                    return deps;
            };

        } catch (Exception e) {
            log.error("Could not read pom in jar {}", pomFile);
            return null;
        }

    }

    @Override
    public ArtifactLocation resolveArtifactLocation(AFReleaseId releaseId) {
        log.debug("resolve location {}", releaseId);
        Optional<URL> url = tryInStructuredJar(releaseId);
        if (url.isPresent()) {
            DefaultArtifact artifact = new DefaultArtifact(releaseId.toExternalForm());
            return new ArtifactLocation(artifact.setFile(new File(url.get().toString())), url.get(), true);
        }
        return null;

    }

    @Override
    public Artifact resolveArtifact(AFReleaseId releaseId) {
        Optional<URL> url = tryInStructuredJar(releaseId);
        if (url.isPresent()) {
            log.info("Resolved in jar repository {}", url);
            DefaultArtifact artifact = new DefaultArtifact(releaseId.toExternalForm());
            return artifact.setFile(new File(url.get().toString()));
        }
        return null;
    }

    protected Optional<URL> tryInStructuredJar(String artifactName) {
        for (URL inJarURL : jarRepository) {
            if (inJarURL.getFile().endsWith(artifactName)) {
                return Optional.of(inJarURL);
            }
        }
        return Optional.empty();
    }

    protected Optional<URL> tryInStructuredJar(AFReleaseId releaseId) {
        return tryInStructuredJar(toFile(releaseId, "jar"));
    }

    private String toFile(AFReleaseId releaseId, String type) {
        return releaseId.getArtifactId() + "-" + releaseId.getVersion() + "." + type;
    }

    @Override
    public List<DependencyDescriptor> getArtifactDependecies(String artifactName) {
        AFReleaseId releaseId = new AFReleaseIdImpl(artifactName);
        PomParser builtPomParser = buildPomParser(releaseId);
        return builtPomParser != null ? builtPomParser.getPomDirectDependencies(DependencyFilter.COMPILE_FILTER) : emptyList();
    }

    @Override
    public List<DependencyDescriptor> getPomDirectDependencies(DependencyFilter dependencyFilter) {
        return (pomParser != null) ? pomParser.getPomDirectDependencies(dependencyFilter) : emptyList();
    }

}
