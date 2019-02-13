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
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.ResourceLoaderConfig;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Nikola Koevski
 */
public class MqttBrokerProcessEnginePlugin extends AbstractProcessEnginePlugin {

  private static final long serialVersionUID = 1L;
  private final Logger logger = getLogger(this.getClass());

  public final Server mqttBroker;
  IConfig classPathConfig = new ResourceLoaderConfig(new ClasspathResourceLoader());

  public MqttBrokerProcessEnginePlugin() {
    this(new Server());
  }

  public MqttBrokerProcessEnginePlugin(Server mqttBroker) {
    this.mqttBroker = mqttBroker;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    customPreBPMNParseListeners(processEngineConfiguration)
      .add(new RegisterExternalTaskBpmnParseListener(this.mqttBroker));
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    try {

      this.mqttBroker.startServer(classPathConfig, Collections.singletonList(new PublishListener()));

      logger.info("MQTT Broker started");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("Stopping broker");
        mqttBroker.stopServer();
        logger.info("Broker stopped");
      }));

    } catch (IOException e) {
      logger.error("MQTT Broket start failed: " + e.getMessage());
    }
  }

  private static List<BpmnParseListener> customPreBPMNParseListeners(final ProcessEngineConfigurationImpl processEngineConfiguration) {
    if (processEngineConfiguration.getCustomPreBPMNParseListeners() == null) {
      processEngineConfiguration.setCustomPreBPMNParseListeners(new ArrayList<BpmnParseListener>());
    }
    return processEngineConfiguration.getCustomPreBPMNParseListeners();
  }
}
