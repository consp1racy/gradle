/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.language.nativeplatform.internal.incremental;

import org.gradle.internal.FileUtils;
import org.gradle.language.nativeplatform.internal.Include;
import org.gradle.language.nativeplatform.internal.IncludeDirectives;
import org.gradle.language.nativeplatform.internal.IncludeType;
import org.gradle.language.nativeplatform.internal.Macro;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSourceIncludesResolver implements SourceIncludesResolver {
    private final List<File> includePaths;
    private final Map<File, Map<String, Boolean>> includeRoots;

    public DefaultSourceIncludesResolver(List<File> includePaths) {
        this.includePaths = includePaths;
        this.includeRoots = new HashMap<File, Map<String, Boolean>>();
    }

    @Override
    public IncludeResolutionResult resolveInclude(File sourceFile, Include include, List<IncludeDirectives> visibleIncludeDirectives) {
        BuildableResult resolvedSourceIncludes = new BuildableResult(include.getValue());
        if (include.getType() == IncludeType.SYSTEM) {
            searchForDependency(includePaths, include.getValue(), resolvedSourceIncludes);
        } else if (include.getType() == IncludeType.QUOTED) {
            List<File> quotedSearchPath = prependSourceDir(sourceFile, includePaths);
            searchForDependency(quotedSearchPath, include.getValue(), resolvedSourceIncludes);
        } else if (include.getType() == IncludeType.MACRO) {
            resolveMacroInclude(sourceFile, visibleIncludeDirectives, include.getValue(), resolvedSourceIncludes);
        } else {
            resolvedSourceIncludes.unresolved();
        }

        return resolvedSourceIncludes;
    }

    private void resolveMacroInclude(File sourceFile, List<IncludeDirectives> visibleIncludeDirectives, String macroName, BuildableResult resolvedSourceIncludes) {
        boolean found = false;
        for (IncludeDirectives includeDirectives : visibleIncludeDirectives) {
            for (Macro macro : includeDirectives.getMacros()) {
                if (macroName.equals(macro.getName())) {
                    found = true;
                    if (macro.getType() == IncludeType.QUOTED) {
                        List<File> quotedSearchPath = prependSourceDir(sourceFile, includePaths);
                        searchForDependency(quotedSearchPath, macro.getValue(), resolvedSourceIncludes);
                    } else if (macro.getType() == IncludeType.MACRO) {
                        resolveMacroInclude(sourceFile, visibleIncludeDirectives, macro.getValue(), resolvedSourceIncludes);
                    } else {
                        // TODO - handle system includes, which also need to be expanded when the value of a macro
                        resolvedSourceIncludes.unresolved();
                    }
                }
            }
        }
        if (!found) {
            resolvedSourceIncludes.unresolved();
        }
    }

    private List<File> prependSourceDir(File sourceFile, List<File> includePaths) {
        List<File> quotedSearchPath = new ArrayList<File>(includePaths.size() + 1);
        quotedSearchPath.add(sourceFile.getParentFile());
        quotedSearchPath.addAll(includePaths);
        return quotedSearchPath;
    }

    private void searchForDependency(List<File> searchPath, String include, BuildableResult dependencies) {
        for (File searchDir : searchPath) {
            File candidate = new File(searchDir, include);

            Map<String, Boolean> searchedIncludes = includeRoots.get(searchDir);
            if (searchedIncludes == null) {
                searchedIncludes = new HashMap<String, Boolean>();
                includeRoots.put(searchDir, searchedIncludes);
            }
            dependencies.searched(candidate);
            if (searchedIncludes.containsKey(include)) {
                if (searchedIncludes.get(include)) {
                    dependencies.resolved(FileUtils.canonicalize(candidate));
                    return;
                }
                continue;
            }

            boolean found = candidate.isFile();
            searchedIncludes.put(include, found);

            if (found) {
                dependencies.resolved(FileUtils.canonicalize(candidate));
                return;
            }
        }
    }

    private static class BuildableResult implements IncludeResolutionResult {
        private final List<File> files = new ArrayList<File>();
        private final List<File> candidates = new ArrayList<File>();
        private final String include;
        private boolean missing;

        BuildableResult(String include) {
            this.include = include;
        }

        void searched(File candidate) {
            candidates.add(candidate);
        }

        void resolved(File file) {
            files.add(file);
        }

        void unresolved() {
            missing = true;
        }

        @Override
        public String getInclude() {
            return include;
        }

        @Override
        public boolean isComplete() {
            return !missing;
        }

        @Override
        public List<File> getFiles() {
            return files;
        }

        @Override
        public List<File> getCheckedLocations() {
            return candidates;
        }
    }
}
