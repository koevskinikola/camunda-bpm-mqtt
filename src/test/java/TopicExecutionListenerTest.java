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
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Namespace;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Nikola Koevski
 */
@Deployment(resources = "Process.bpmn")
public class TopicExecutionListenerTest {

  public static String topicName;

  private static ProcessEngineConfiguration CONFIGURATION = new StandaloneInMemProcessEngineConfiguration() {
    {
      this.getProcessEnginePlugins().add(new MqttBrokerProcessEngineTestPlugin());
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
  public void testExtTaskTopic() {
    processEngineRule.getRuntimeService().startProcessInstanceByKey("process");

    List<ExternalTask> externalTasks = processEngineRule.getExternalTaskService().createExternalTaskQuery().list();

    assertThat(externalTasks.size(), is(1));

    assertThat(topicName, is(notNullValue()));
    assertThat(externalTasks.get(0).getTopicName(), equalTo("externalTaskTopic"));
  }


  public static class MqttBrokerProcessEngineTestPlugin extends AbstractProcessEnginePlugin {

    public static final Namespace CAMUNDA_BPMN_EXTENSIONS_NS = new Namespace(BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS);
    public static final String TYPE = "type";
    public static final String START_EVENT = "start";

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
      customPreBPMNParseListeners(processEngineConfiguration)
        .add(new RegisterExternalTaskBpmnParseListener());
    }

    private static List<BpmnParseListener> customPreBPMNParseListeners(final ProcessEngineConfigurationImpl processEngineConfiguration) {
      if (processEngineConfiguration.getCustomPreBPMNParseListeners() == null) {
        processEngineConfiguration.setCustomPreBPMNParseListeners(new ArrayList<BpmnParseListener>());
      }
      return processEngineConfiguration.getCustomPreBPMNParseListeners();
    }

    private static class RegisterExternalTaskBpmnParseListener extends AbstractBpmnParseListener {
      @Override
      public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {

        String type = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE);
        if (type.equalsIgnoreCase("external")) {

          activity.addListener(START_EVENT, (ExecutionListener) execution -> {

            Context.getCommandContext().getTransactionContext()
              .addTransactionListener(TransactionState.COMMITTED, commandContext -> {

              ExternalTask externalTask = execution.getProcessEngineServices()
                .getExternalTaskService()
                .createExternalTaskQuery()
                .executionId(execution.getId())
                .singleResult();

              if (externalTask != null) {
                topicName = externalTask.getTopicName();
              }
            });
          });
        }
      }
    }
  }
}
