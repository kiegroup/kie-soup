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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.appformer.maven.support.AFReleaseId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplodedJarArtifactResolver extends AbstractFilesArtifactResolver {

    private static final String EXPLODED_JAR_FOLDER = System.getProperty("org.kie.maven.resolver.folder", "/workspace");

    private static final Logger log = LoggerFactory.getLogger(ExplodedJarArtifactResolver.class);

    ExplodedJarArtifactResolver(ClassLoader classLoader, AFReleaseId releaseId) {
        super(classLoader, releaseId);
    }


    @Override
    protected List<URL> buildResources(Predicate<String> predicate) {

        Path base = Paths.get(EXPLODED_JAR_FOLDER);
        if(!Files.isDirectory(base)) {
            log.error("Exploded jar autoscan folder failed. {} does not exist or it is not a folder", base);
            return Collections.emptyList();
        }
        try (Stream<Path> files = Files.walk(base)){
            List<Path> candidates = files.filter(Files::isRegularFile).collect(Collectors.toList());
            List<URL> urls = new ArrayList<>();
            for(Path candidate : candidates) {
                if(predicate.test(candidate.toString())) {
                    try {
                        urls.add(candidate.toUri().toURL());
                        log.debug("Found resource in exploded jar {}", candidate);
                    } catch (MalformedURLException e) {
                        log.warn("Failed to convert candidate to proper URL resource {}", candidate, e);
                    }
                }
            }
            return urls;
        } catch(IOException e) {
            log.error("Error trying to scan for resources in exploded jar {}", EXPLODED_JAR_FOLDER, e);
            return Collections.emptyList();
        }

    }

    protected URL getURL(String path) {
        try {
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
