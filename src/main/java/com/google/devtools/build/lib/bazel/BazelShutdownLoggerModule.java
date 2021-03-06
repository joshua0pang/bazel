// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.bazel;

import com.google.devtools.build.lib.analysis.BlazeDirectories;
import com.google.devtools.build.lib.analysis.BlazeVersionInfo;
import com.google.devtools.build.lib.runtime.BlazeModule;
import com.google.devtools.build.lib.util.Clock;
import com.google.devtools.common.options.OptionsProvider;

import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Shutdown log output when Bazel runs in batch mode
 */
public class BazelShutdownLoggerModule extends BlazeModule {
  private Logger globalLogger;

  @Override
  public void blazeStartup(OptionsProvider startupOptions, BlazeVersionInfo versionInfo,
      UUID instanceId, BlazeDirectories directories, Clock clock) {
    LogManager.getLogManager().reset();
    globalLogger = Logger.getGlobal();
    globalLogger.setLevel(java.util.logging.Level.OFF);
  }
}
