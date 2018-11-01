/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.appformer.maven.support;

import static org.junit.Assert.assertEquals;

public final class ParserTestUtil {

    public static void assertPomModel(final PomModel pomModel) {
        assertEquals( "groupId", pomModel.getReleaseId().getGroupId() );
        assertEquals( "artifactId", pomModel.getReleaseId().getArtifactId() );
        assertEquals( "version", pomModel.getReleaseId().getVersion() );

        assertEquals( "parentGroupId", pomModel.getParentReleaseId().getGroupId() );
        assertEquals( "parentArtifactId", pomModel.getParentReleaseId().getArtifactId() );
        assertEquals( "parentVersion", pomModel.getParentReleaseId().getVersion() );

        assertEquals( 1, pomModel.getDependencies().size() );
        AFReleaseId dep = pomModel.getDependencies().iterator().next();
        assertEquals( "dep1GroupId", dep.getGroupId() );
        assertEquals( "dep1ArtifactId", dep.getArtifactId() );
        assertEquals( "dep1Version", dep.getVersion() );
    }

    public ParserTestUtil() {
        // It is forbidden to instantiate util classes.
    }
}
