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

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Nikola Koevski
 */
public class PublishListener extends AbstractInterceptHandler {

  private static final long serialVersionUID = 1L;
  private final Logger logger = getLogger(this.getClass());

  @Override
  public String getID() {
    return "MQTT Publish Listener";
  }

  @Override
  public void onPublish(InterceptPublishMessage msg) {
//    int decodedPayload = msg.getPayload().readInt();
    logger.info("External Task on topic: ");// + msg.getTopicName() + " available (" + decodedPayload + ")");
  }


}