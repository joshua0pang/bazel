// Copyright 2015 Google Inc. All rights reserved.
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

package com.google.devtools.build.buildjar.genclass;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;

/** The options for a {@GenClass} action. */
public final class GenClassOptions {

  /** A builder for {@link GenClassOptions}. */
  public static final class Builder {
    private Path manifest;
    private Path classJar;
    private Path outputJar;
    private Path tempDir;

    public Builder() {}

    public void setManifest(Path manifest) {
      this.manifest = manifest;
    }

    public void setClassJar(Path classJar) {
      this.classJar = classJar;
    }

    public void setOutputJar(Path outputJar) {
      this.outputJar = outputJar;
    }

    public void setTempDir(Path tempDir) {
      this.tempDir = tempDir;
    }

    GenClassOptions build() {
      return new GenClassOptions(manifest, classJar, outputJar, tempDir);
    }
  }

  private final Path manifest;
  private final Path classJar;
  private final Path outputJar;
  private final Path tempDir;

  private GenClassOptions(Path manifest, Path classJar, Path outputJar, Path tempDir) {
    this.manifest = checkNotNull(manifest);
    this.classJar = checkNotNull(classJar);
    this.outputJar = checkNotNull(outputJar);
    this.tempDir = checkNotNull(tempDir);
  }

  /** The path to the compilation manifest proto. */
  public Path manifest() {
    return manifest;
  }

  /** The path to the compilation's class jar. */
  public Path classJar() {
    return classJar;
  }

  /** The path to write the output to. */
  public Path outputJar() {
    return outputJar;
  }

  /** The path to the temp directory. */
  public Path tempDir() {
    return tempDir;
  }

  /** Returns a builder for {@link GenClassOptions}. */
  public static Builder builder() {
    return new Builder();
  }
}
