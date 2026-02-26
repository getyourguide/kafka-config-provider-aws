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

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.provider.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SecretsManagerConfigProvider implements ConfigProvider {
  private static final Logger log = LoggerFactory.getLogger(SecretsManagerConfigProvider.class);
  SecretsManagerConfigProviderConfig config;
  SecretsManagerFactory secretsManagerFactory = new SecretsManagerFactoryImpl();
  SecretsManagerClient secretsManager;
  ObjectMapper mapper = new ObjectMapper();

  @Override
  public ConfigData get(String path) {
    return get(path, Collections.emptySet());
  }

  @Override
  public ConfigData get(String p, Set<String> keys) {
    log.info("get() - path = '{}' keys = '{}'", p, keys);

    Path path = (null != this.config.prefix && !this.config.prefix.isEmpty()) ?
        Paths.get(this.config.prefix, p) :
        Paths.get(p);

    try {
      log.debug("Requesting {} from Secrets Manager", path);
      GetSecretValueRequest request = GetSecretValueRequest.builder()
          .secretId(path.toString())
          .build();

      GetSecretValueResponse result = this.secretsManager.getSecretValue(request);
      ObjectNode node;

      if (null != result.secretString()) {
        node = mapper.readValue(result.secretString(), ObjectNode.class);
      } else if (null != result.secretBinary()) {
        node = mapper.readValue(result.secretBinary().asByteArray(), ObjectNode.class);
      } else {
        throw new ConfigException("");
      }

      Set<String> propertiesToRead = (null == keys || keys.isEmpty()) ? ImmutableSet.copyOf(node.fieldNames()) : keys;
      Map<String, String> results = new LinkedHashMap<>(propertiesToRead.size());
      for (String propertyName : propertiesToRead) {
        JsonNode propertyNode = node.get(propertyName);
        if (null != propertyNode && !propertyNode.isNull()) {
          results.put(propertyName, propertyNode.textValue());
        }
      }
      return new ConfigData(results, config.minimumSecretTTL);
    } catch (DecryptionFailureException ex) {
      throw createException(ex, "Could not decrypt secret '%s'", path);
    } catch (ResourceNotFoundException ex) {
      throw createException(ex, "Could not find secret '%s'", path);
    } catch (IOException ex) {
      throw createException(ex, "Exception thrown while reading secret '%s'", path);
    }
  }

  ConfigException createException(Throwable cause, String message, Object... args) {
    String exceptionMessage = String.format(message, args);
    ConfigException configException = new ConfigException(exceptionMessage);
    configException.initCause(cause);
    return configException;
  }

  @Override
  public void close() throws IOException {
    if (null != this.secretsManager) {
      this.secretsManager.close();
    }
  }

  @Override
  public void configure(Map<String, ?> settings) {
    this.config = new SecretsManagerConfigProviderConfig(settings);
    this.secretsManager = this.secretsManagerFactory.create(this.config);
  }

  public static ConfigDef config() {
    return SecretsManagerConfigProviderConfig.config();
  }
}
