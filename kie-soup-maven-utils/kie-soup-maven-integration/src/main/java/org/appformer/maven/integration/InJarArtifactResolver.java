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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.appformer.maven.support.AFReleaseId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InJarArtifactResolver extends AbstractFilesArtifactResolver {

    private static final Logger log = LoggerFactory.getLogger(InJarArtifactResolver.class);


    InJarArtifactResolver(ClassLoader classLoader, AFReleaseId releaseId) {
        super(classLoader, releaseId);
        
    }

    @Override
    protected List<URL> buildResources(Predicate<String> predicate) {
        URL resourceURL = this.getClassLoader().getResource("");
        List<URL> resources = new ArrayList<>();
        try (InputStream is = resourceURL.openStream();
                ZipInputStream stream = new ZipInputStream(is);) {

            ZipEntry entry = null;
            while ((entry = stream.getNextEntry()) != null) {
                if (predicate.test(entry.getName())) {
                    resources.add(this.getClassLoader().getResource(entry.getName()));
                }
            }

            log.debug("Found in jar repository {}", resources);
        } catch (IOException e) {
            log.error("Error trying to open URL: {}", resourceURL);
        }
        return resources;
    }

    protected URL getURL(String path) {
        return getClassLoader().getResource(path);
    }

}
