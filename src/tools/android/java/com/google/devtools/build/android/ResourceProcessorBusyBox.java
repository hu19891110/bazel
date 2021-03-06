// Copyright 2017 The Bazel Authors. All rights reserved.
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

package com.google.devtools.build.android;

import com.google.devtools.common.options.EnumConverter;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParser;
import java.nio.file.FileSystems;

/**
 * Provides an entry point for the resource processing stages.
 *
 * <p>A single entry point simplifies the build tool binary configuration and keeps the size of tool
 * jar small, as opposed to multiple tools for each prosess step. It also makes it easy to prototype
 * changes in the resource processing system.
 *
 * <pre>
 * Example Usage:
 *   java/com/google/build/android/ResourceProcessorBusyBox\
 *      --tool PACKAGE\
 *      --sdkRoot path/to/sdk\
 *      --aapt path/to/sdk/aapt\
 *      --annotationJar path/to/sdk/annotationJar\
 *      --adb path/to/sdk/adb\
 *      --zipAlign path/to/sdk/zipAlign\
 *      --androidJar path/to/sdk/androidJar\
 *      --manifestOutput path/to/manifest\
 *      --primaryData path/to/resources:path/to/assets:path/to/manifest\
 *      --data p/t/res1:p/t/assets1:p/t/1/AndroidManifest.xml:p/t/1/R.txt:symbols,\
 *             p/t/res2:p/t/assets2:p/t/2/AndroidManifest.xml:p/t/2/R.txt:symbols\
 *      --packagePath path/to/write/archive.ap_\
 *      --srcJarOutput path/to/write/archive.srcjar
 * </pre>
 */
public class ResourceProcessorBusyBox {
  static enum Tool {
    PACKAGE() {
      @Override
      void call(String[] args) throws Exception {
        AndroidResourceProcessingAction.main(args);
      }
    },
    VALIDATE() {
      @Override
      void call(String[] args) throws Exception {
        AndroidResourceValidatorAction.main(args);
      }
    },
    GENERATE_BINARY_R() {
      @Override
      void call(String[] args) throws Exception {
        RClassGeneratorAction.main(args);
      }
    },
    GENERATE_LIBRARY_R() {
      @Override
      void call(String[] args) throws Exception {
        LibraryRClassGeneratorAction.main(args);
      }
    },
    PARSE() {
      @Override
      void call(String[] args) throws Exception {
        AndroidResourceParsingAction.main(args);
      }
    },
    MERGE() {
      @Override
      void call(String[] args) throws Exception {
        AndroidResourceMergingAction.main(args);
      }
    },
    GENERATE_AAR() {
      @Override
      void call(String[] args) throws Exception {
        AarGeneratorAction.main(args);
      }
    },
    SHRINK() {
      @Override
      void call(String[] args) throws Exception {
        ResourceShrinkerAction.main(args);
      }
    },
    MERGE_MANIFEST() {
      @Override
      void call(String[] args) throws Exception {
        ManifestMergerAction.main(args);
      }
    };

    abstract void call(String[] args) throws Exception;
  }

  /** Converter for the Tool enum. */
  public static final class ToolConverter extends EnumConverter<Tool> {

    public ToolConverter() {
      super(Tool.class, "resource tool");
    }
  }

  /** Flag specifications for this action. */
  public static final class Options extends OptionsBase {
    @Option(
      name = "tool",
      defaultValue = "null",
      converter = ToolConverter.class,
      category = "input",
      help =
          "The processing tool to execute. "
              + "Valid tools: PACKAGE, VALIDATE, GENERATE_BINARY_R, GENERATE_LIBRARY_R, PARSE, "
              + "MERGE, GENERATE_AAR, SHRINK, MERGE_MANIFEST."
    )
    public Tool tool;
  }

  public static void main(String[] args) throws Exception {
    OptionsParser optionsParser = OptionsParser.newOptionsParser(Options.class);
    optionsParser.setAllowResidue(true);
    optionsParser.enableParamsFileSupport(FileSystems.getDefault());
    optionsParser.parse(args);
    Options options = optionsParser.getOptions(Options.class);
    options.tool.call(optionsParser.getResidue().toArray(new String[0]));
  }
}
