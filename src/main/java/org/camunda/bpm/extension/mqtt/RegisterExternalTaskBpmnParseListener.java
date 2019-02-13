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
package org.camunda.bpm.extension.mqtt;

import io.moquette.broker.Server;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Namespace;

/**
 * @author Nikola Koevski
 */
public class RegisterExternalTaskBpmnParseListener extends AbstractBpmnParseListener {

  protected static final Namespace CAMUNDA_BPMN_EXTENSIONS_NS = new Namespace(BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS);
  protected static final String TYPE = "type";
  protected static final String START_EVENT = "start";
  protected static final String EXTERNAL_TASK = "external";

  public Server mqttBroker;

  public RegisterExternalTaskBpmnParseListener(Server mqttBroker) {
    this.mqttBroker = mqttBroker;
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {

    String type = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE);

    if (type.equalsIgnoreCase(EXTERNAL_TASK)) {
      activity.addListener(START_EVENT, new MqttExternalTaskCreatedListener(mqttBroker));
    }
  }
}
