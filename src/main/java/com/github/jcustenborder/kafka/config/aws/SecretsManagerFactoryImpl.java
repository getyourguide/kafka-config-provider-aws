/**
 * Copyright © 2021 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.config.aws;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;

class SecretsManagerFactoryImpl implements SecretsManagerFactory {
  @Override
  public SecretsManagerClient create(SecretsManagerConfigProviderConfig config) {
    SecretsManagerClientBuilder builder = SecretsManagerClient.builder();

    if (null != config.region && !config.region.isEmpty()) {
      builder = builder.region(Region.of(config.region));
    }
    if (null != config.credentials) {
      builder = builder.credentialsProvider(StaticCredentialsProvider.create(config.credentials));
    }

    return builder.build();
  }
}
