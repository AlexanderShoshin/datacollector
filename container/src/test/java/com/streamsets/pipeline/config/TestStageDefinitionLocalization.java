/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.impl.LocaleInContext;

import com.streamsets.pipeline.el.ElConstantDefinition;
import com.streamsets.pipeline.el.ElFunctionDefinition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class TestStageDefinitionLocalization {

  @After
  public void cleanUp() {
    LocaleInContext.set(null);
  }

  private static final StageLibraryDefinition MOCK_LIB_DEF =
      new StageLibraryDefinition(TestStageDefinitionLocalization.class.getClassLoader(), "mock", "MOCK", new Properties());

  @SuppressWarnings("unchecked")
  private StageDefinition createStageDefinition() {
    List<ConfigDefinition> configs = new ArrayList<>();
    configs.add(new ConfigDefinition("c1", ConfigDef.Type.STRING, "Config1Label", "Config1Description", "default",
                                     true, "GROUP", "c1", null, null, null, 0,
                                     Collections.<ElFunctionDefinition>emptyList(),
                                     Collections.<ElConstantDefinition>emptyList(),
                                     0, 0, "mode", 1,
      Collections.<Class> emptyList(), ConfigDef.Evaluation.IMPLICIT, null));
    ModelDefinition model = new ModelDefinition(ModelType.VALUE_CHOOSER, OptionsChooserValues.class.getName(),
                                                ImmutableList.of("OPTION"), ImmutableList.of("Option"), null,  null);
    configs.add(new ConfigDefinition("c2", ConfigDef.Type.MODEL, "Config2Label", "Config2Description", "default",
                                     true, "GROUP", "c2", model, null, null, 0,
                                     Collections.<ElFunctionDefinition>emptyList(),
                                     Collections.<ElConstantDefinition>emptyList(),
                                     0, 0, "mode", 1,
      Collections.<Class> emptyList(), ConfigDef.Evaluation.IMPLICIT, null));
    RawSourceDefinition rawSource = new RawSourceDefinition(TRawSourcePreviewer.class.getName(), "*/*", configs);
    ConfigGroupDefinition configGroup = new ConfigGroupDefinition(ImmutableSet.of("GROUP"),
        (Map)ImmutableMap.of(Groups.class.getName(), ImmutableList.of(Groups.GROUP.name())),
        (List)ImmutableList.of(ImmutableMap.of("label", "Group", "name", "GROUP"))
    );
    StageDefinition def = new StageDefinition(MOCK_LIB_DEF, TProcessor.class, "stage", "1.0.0", "StageLabel",
                                              "StageDescription", StageType.PROCESSOR, true, true, true, configs,
                                              rawSource, "", configGroup, false, 1,
                                              TOutput.class.getName(),
                                              Arrays.asList(ExecutionMode.CLUSTER,
                                                            ExecutionMode.STANDALONE), false);
    return def;
  }

  private void testMessages(Locale locale, Map<String, String> expected) {
    StageDefinition def = createStageDefinition();
    LocaleInContext.set(locale);
    def = def.localize();
    //stage
    Assert.assertEquals(expected.get("stageLabel"), def.getLabel());
    Assert.assertEquals(expected.get("stageDescription"), def.getDescription());

    //error stage
    Assert.assertTrue(def.isErrorStage());

    //stage groups
    Assert.assertEquals(expected.get("stageGroup"), def.getConfigGroupDefinition().getGroupNameToLabelMapList().get(0).get(
        "label"));

    //stage configs
    Assert.assertEquals(expected.get("c1Label"), def.getConfigDefinition("c1").getLabel());
    Assert.assertEquals(expected.get("c1Description"), def.getConfigDefinition("c1").getDescription());
    Assert.assertEquals(expected.get("c2Label"), def.getConfigDefinition("c2").getLabel());
    Assert.assertEquals(expected.get("c2Description"),def.getConfigDefinition("c2").getDescription());
    Assert.assertEquals(expected.get("c2OptionLabel" +
                                     ""),def.getConfigDefinition("c2").getModel().getLabels().get(0));

    //stage raw preview
    Assert.assertEquals(expected.get("c1Label"),
                        def.getRawSourceDefinition().getConfigDefinitions().get(0).getLabel());
    Assert.assertEquals(expected.get("c1Description"),
                        def.getRawSourceDefinition().getConfigDefinitions().get(0).getDescription());
    Assert.assertEquals(expected.get("c2Label"),
                        def.getRawSourceDefinition().getConfigDefinitions().get(1).getLabel());
    Assert.assertEquals(expected.get("c2Description"),
                        def.getRawSourceDefinition().getConfigDefinitions().get(1).getDescription());
    Assert.assertEquals(expected.get("c2OptionLabel"),
                        def.getRawSourceDefinition().getConfigDefinitions().get(1).getModel().getLabels().get(0));

    //stage output streams
    Assert.assertEquals(expected.get("stageOutput"), def.getOutputStreamLabels().get(0));
  }

  private static final Map<String, String> EXPECTED_BUILT_IN = new HashMap<>();

  static {
    EXPECTED_BUILT_IN.put("stageLabel", "StageLabel");
    EXPECTED_BUILT_IN.put("stageDescription", "StageDescription");
    EXPECTED_BUILT_IN.put("errorStageLabel", "ErrorStageLabel");
    EXPECTED_BUILT_IN.put("errorStageDescription", "ErrorStageDescription");
    EXPECTED_BUILT_IN.put("stageGroup", "Group");
    EXPECTED_BUILT_IN.put("c1Label", "Config1Label");
    EXPECTED_BUILT_IN.put("c1Description", "Config1Description");
    EXPECTED_BUILT_IN.put("c2Label", "Config2Label");
    EXPECTED_BUILT_IN.put("c2Description", "Config2Description");
    EXPECTED_BUILT_IN.put("c2OptionLabel", "Option");
    EXPECTED_BUILT_IN.put("stageOutput", "Output");
  }

  @Test
  public void testBuiltInMessages() throws Exception {
    testMessages(Locale.getDefault(), EXPECTED_BUILT_IN);
  }

  private static final Map<String, String> EXPECTED_RESOURCE_BUNDLE = new HashMap<>();

  static {
    EXPECTED_RESOURCE_BUNDLE.put("stageLabel", "XStageLabel");
    EXPECTED_RESOURCE_BUNDLE.put("stageDescription", "XStageDescription");
    EXPECTED_RESOURCE_BUNDLE.put("errorStageLabel", "XErrorStageLabel");
    EXPECTED_RESOURCE_BUNDLE.put("errorStageDescription", "XErrorStageDescription");
    EXPECTED_RESOURCE_BUNDLE.put("stageGroup", "XGroup");
    EXPECTED_RESOURCE_BUNDLE.put("c1Label", "XConfig1Label");
    EXPECTED_RESOURCE_BUNDLE.put("c1Description", "XConfig1Description");
    EXPECTED_RESOURCE_BUNDLE.put("c2Label", "XConfig2Label");
    EXPECTED_RESOURCE_BUNDLE.put("c2Description", "XConfig2Description");
    EXPECTED_RESOURCE_BUNDLE.put("c2OptionLabel", "XOption");
    EXPECTED_RESOURCE_BUNDLE.put("stageOutput", "XOutput");
  }

  @Test
  public void testResourceBundleMessages() throws Exception {
    testMessages(new Locale("xyz"), EXPECTED_RESOURCE_BUNDLE);
  }

}
