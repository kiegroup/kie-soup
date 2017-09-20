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

package org.kie.soup.project.datamodel.oracle;

import java.util.List;
import java.util.Map;

public interface PackageDataModelOracle extends ProjectDataModelOracle {

    void setPackageName( String packageName );

    void addPackageWorkbenchEnumDefinitions( Map<String, String[]> workbenchEnumDefinitions );

    void addPackageGlobals( Map<String, String> globalss );

    <T> void addExtensions( ExtensionKind<T> kind, List<T> elements );

    void addExtensions( Map<ExtensionKind<?>, List<?>> packageElements );

    String getPackageName();

    Map<String, String[]> getPackageWorkbenchDefinitions();

    Map<String, String> getPackageGlobals();

    <T> List<T> getExtensions( ExtensionKind<T> kind );

    Map<ExtensionKind<?>, List<?>> getAllExtensions();

}
