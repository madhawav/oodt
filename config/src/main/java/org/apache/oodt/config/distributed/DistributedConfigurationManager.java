/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.config.distributed;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.Constants.Properties;
import org.apache.oodt.config.utils.CuratorUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;
import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

/**
 * Distributed configuration manager implementation. This class make use of a {@link CuratorFramework} instance to connect
 * to zookeeper
 *
 * @author Imesha Sudasingha.
 */
public class DistributedConfigurationManager extends ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(DistributedConfigurationManager.class);

    /** Variables required to connect to zookeeper */
    private String connectString;
    private CuratorFramework client;

    public DistributedConfigurationManager(String component, List<String> propertiesFiles) {
        super(component, propertiesFiles);

        if (System.getProperty(ZK_PROPERTIES_FILE) == null && System.getProperty(Constants.Properties.ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires system properties " + ZK_PROPERTIES_FILE + " or " + ZK_CONNECT_STRING + " to be set");
        }

        if (System.getProperty(ZK_PROPERTIES_FILE) != null) {
            try {
                CuratorUtils.loadZookeeperProperties();
            } catch (IOException e) {
                logger.error("Error occurred when loading properties from properties file");
            }
        }

        if (System.getProperty(Constants.Properties.ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires a proper connect string to connect to zookeeper ensemble");
        }

        connectString = System.getProperty(Constants.Properties.ZK_CONNECT_STRING);
        logger.info("Using zookeeper connect string : {}", connectString);

        startZookeeper();
    }

    /**
     * Creates a {@link CuratorFramework} instance and start it. This method will wait a maximum amount of
     * {@link Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
     */
    private void startZookeeper() {
        int connectionTimeoutMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "15"));
        int sessionTimeoutMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "60"));
        int retryInitialWaitMs = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "1000"));
        int maxRetryCount = Integer.parseInt(System.getProperty(Properties.ZK_CONNECTION_TIMEOUT, "3"));
        int startupTimeOutMs = Integer.parseInt(System.getProperty(Properties.ZK_STARTUP_TIMEOUT, "30000"));

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(retryInitialWaitMs, maxRetryCount))
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs);

        /*
         * If authorization information is available, those will be added to the client. NOTE: These auth info are
         * for access control, therefore no authentication will happen when the client is being started. These
         * info will only be required whenever a client is accessing an already create ZNode. For another client of
         * another node to make use of a ZNode created by this node, it should also provide the same auth info.
         */
        if (System.getProperty(Properties.ZK_USERNAME) != null && System.getProperty(Properties.ZK_PASSWORD) != null) {
            String authenticationString = System.getProperty(Properties.ZK_USERNAME) + ":" + System.getProperty(Properties.ZK_PASSWORD);
            builder.authorization("digest", authenticationString.getBytes())
                    .aclProvider(new ACLProvider() {
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        public List<ACL> getAclForPath(String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }

        client = builder.build();
        logger.debug("CuratorFramework client built successfully with connectString: {}, sessionTimeout: {} and connectionTimeout: {}",
                connectString, sessionTimeoutMs, connectionTimeoutMs);

        client.start();
        logger.info("Curator framework start operation invoked");

        try {
            logger.info("Waiting to connect to zookeeper, startupTimeout : {}", startupTimeOutMs);
            client.blockUntilConnected(startupTimeOutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            logger.error("Interrupted while waiting to connect zookeeper (connectString : {}) : {}", ex, connectString);
        }

        if (!client.getZookeeperClient().isConnected()) {
            throw new IllegalStateException("Could not connect to ZooKeeper : " + connectString);
        }

        logger.info("CuratorFramework client started successfully");
    }

    @Override
    public String getProperty(String key) {
        // Todo Implement using curator
        return null;
    }

    @Override
    public void loadProperties() {
        // todo Implement the logic with Curator
    }

    public File getPropertiesFile(String filePath) {
        return null;
    }

    public File getConfigurationFile(String filePath) {
        return null;
    }

    public void publishConfiguration() {
        for (String propertyFile : propertiesFiles) {

        }
    }
}
