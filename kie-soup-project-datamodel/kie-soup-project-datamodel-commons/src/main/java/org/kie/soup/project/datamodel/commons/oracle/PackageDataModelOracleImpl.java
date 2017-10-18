/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.soup.project.datamodel.commons.oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.soup.project.datamodel.oracle.ExtensionKind;
import org.kie.soup.project.datamodel.oracle.PackageDataModelOracle;

/**
 * Default implementation of DataModelOracle
 */
public class PackageDataModelOracleImpl extends ModuleDataModelOracleImpl implements PackageDataModelOracle {

    //Package for which this DMO relates
    private String packageName = "";

    // Package-level enumeration definitions derived from "Workbench" enumerations.
    private Map<String, String[]> packageWorkbenchEnumDefinitions = new HashMap<>();

    // Package-level map of Globals {alias, class name}.
    private Map<String, String> packageGlobalTypes = new HashMap<>();

    private Map<ExtensionKind<?>, List<?>> packageElements = new HashMap<>();

    @Override
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void addPackageWorkbenchEnumDefinitions(final Map<String, String[]> dataEnumLists) {
        this.packageWorkbenchEnumDefinitions.putAll(dataEnumLists);
    }

    @Override
    public void addPackageGlobals(final Map<String, String> packageGlobalTypes) {
        this.packageGlobalTypes.putAll(packageGlobalTypes);
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public Map<String, String[]> getPackageWorkbenchDefinitions() {
        return this.packageWorkbenchEnumDefinitions;
    }

    @Override
    public Map<String, String> getPackageGlobals() {
        return this.packageGlobalTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void addExtensions(ExtensionKind<T> kind, List<T> elements) {
        @SuppressWarnings("rawtypes")
        List list = packageElements.computeIfAbsent(kind, k -> new ArrayList<>());
        list.addAll(elements);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addExtensions(Map<ExtensionKind<?>, List<?>> packageElements) {
        packageElements
                .forEach((k, v) -> {
                    @SuppressWarnings("rawtypes")
                    List list = this.packageElements.computeIfAbsent(k, k1 -> new ArrayList<>());
                    list.addAll(v);
                });
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> List<T> getExtensions(ExtensionKind<T> kind) {
        return (List<T>) packageElements.computeIfAbsent(kind, k -> new ArrayList<>());
    }

    @Override
    public Map<ExtensionKind<?>, List<?>> getAllExtensions() {
        return packageElements;
    }
}

