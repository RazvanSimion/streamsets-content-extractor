/*
 * Copyright 2017 StreamSets Inc.
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
package ro.trc.streamsets.stage.processor.sample;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;

@StageDef(
    version = 1,
    label = "Content Extract Processor",
    description = "",
    icon = "default.png",
    onlineHelpRefUrl = ""
)
@ConfigGroups(Groups.class)
@GenerateResourceBundle
public class SampleDProcessor extends SampleProcessor {

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "default",
      label = "Sample Config",
      displayPosition = 10,
      group = "SAMPLE"
  )
  public String config;

  /** {@inheritDoc} */
  @Override
  public String getConfig() {
    return config;
  }

}
