/*
 * Copyright © 2019 camunda services GmbH and various authors (info@camunda.com)
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
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.mqtt.MqttBrokerProcessEnginePlugin;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Nikola Koevski
 */
@Deployment(resources = "Process.bpmn")
public class MqttBrokerTest {

  private static ProcessEngineConfiguration CONFIGURATION = new StandaloneInMemProcessEngineConfiguration() {
    {
      this.getProcessEnginePlugins().add(new MqttBrokerProcessEnginePlugin());
      this.jobExecutorActivate = false;
      this.isDbMetricsReporterActivate = false;
    }
  };

  private static ProcessEngine processEngine;

  public static ProcessEngine processEngine() {
    if (processEngine == null) {
      processEngine = CONFIGURATION.buildProcessEngine();
    }
    return processEngine;
  }

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule(processEngine());

  @Test
  public void testMqttBroker() {



    processEngineRule.getRuntimeService().startProcessInstanceByKey("process");

    List<ExternalTask> externalTasks = processEngineRule.getExternalTaskService().createExternalTaskQuery().list();

    assertThat(externalTasks.size(), is(1));
  }
}
