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
 *
 */

package org.kie.soup.project.datamodel.oracle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.kie.soup.commons.validation.PortablePreconditions;

/**
 * Portable representation of an annotation
 */
public class Annotation {

    private String qualifiedTypeName;
    private Map<String, Object> parameters = new HashMap<>();

    public Annotation() {
        //Needed for Errai marshalling
    }

    public Annotation(final String qualifiedTypeName) {
        PortablePreconditions.checkNotNull("qualifiedTypeName",
                                           qualifiedTypeName);
        this.qualifiedTypeName = qualifiedTypeName;
    }

    public String getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void addParameter(final String name,
                             final Object value) {
        PortablePreconditions.checkNotNull("name",
                                           name);
        this.parameters.put(name,
                            value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Annotation that = (Annotation) o;
        return Objects.equals(qualifiedTypeName, that.qualifiedTypeName) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(qualifiedTypeName);
        result = ~~result;
        result = 31 * result + Objects.hashCode(parameters);
        result = ~~result;
        return result;
    }
}
