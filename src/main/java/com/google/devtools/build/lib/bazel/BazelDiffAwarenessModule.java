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
package com.google.devtools.build.lib.bazel;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.runtime.BlazeModule;
import com.google.devtools.build.lib.skyframe.DiffAwareness;
import com.google.devtools.build.lib.skyframe.LocalDiffAwareness;

/**
 * Provides the {@link DiffAwareness} implementation that uses the Java watch service.
 */
public class BazelDiffAwarenessModule extends BlazeModule {
  @Override
  public Iterable<DiffAwareness.Factory> getDiffAwarenessFactories(boolean watchFS) {
    ImmutableList.Builder<DiffAwareness.Factory> builder = ImmutableList.builder();
    if (watchFS) {
      builder.add(new LocalDiffAwareness.Factory(ImmutableList.<String>of()));
    }
    return builder.build();
  }
}
