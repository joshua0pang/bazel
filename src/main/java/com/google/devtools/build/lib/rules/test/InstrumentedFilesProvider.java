// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.rules.test;

import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.analysis.TransitiveInfoProvider;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;

import java.util.Map;

/**
 * A provider of instrumented file sources and instrumentation metadata.
 */
public interface InstrumentedFilesProvider extends TransitiveInfoProvider {

  /**
   * Returns a collection of source files for instrumented binaries.
   */
  NestedSet<Artifact> getInstrumentedFiles();

  /**
   * Returns a collection of instrumentation metadata files.
   */
  NestedSet<Artifact> getInstrumentationMetadataFiles();

  NestedSet<Artifact> getBaselineCoverageArtifacts();

  /**
   * Returns environment variables which should be set for coverage to function.
   */
  Map<String, String> getExtraEnv();
}
