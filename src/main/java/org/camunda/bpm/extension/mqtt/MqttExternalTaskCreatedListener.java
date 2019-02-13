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
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Nikola Koevski
 */
public class MqttExternalTaskCreatedListener implements ExecutionListener {

  Server mqttBroker;

  public MqttExternalTaskCreatedListener(Server mqttBroker) {
    this.mqttBroker = mqttBroker;
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {

    Context.getCommandContext().getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, commandContext -> {

        ExternalTask externalTask = execution.getProcessEngineServices()
          .getExternalTaskService()
          .createExternalTaskQuery()
          .executionId(execution.getId())
          .singleResult();

      if (externalTask != null) {
        String topicName = externalTask.getTopicName();

        MqttPublishMessage mqttPublishMessage = MqttMessageBuilders.publish()
          .topicName("/" + topicName)
          .retained(false)
          .qos(MqttQoS.EXACTLY_ONCE)
          .payload(Unpooled.copyInt(1))
          .build();

        mqttBroker.internalPublish(mqttPublishMessage, "INTRLPUB");
      }
    });
  }
}
