/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.gravitino.authorization.ranger.integration.test;

import static org.apache.gravitino.Catalog.AUTHORIZATION_PROVIDER;
import static org.apache.gravitino.authorization.ranger.integration.test.RangerITEnv.currentFunName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import org.apache.gravitino.Catalog;
import org.apache.gravitino.Configs;
import org.apache.gravitino.auth.AuthConstants;
import org.apache.gravitino.auth.AuthenticatorType;
import org.apache.gravitino.authorization.Privileges;
import org.apache.gravitino.authorization.SecurableObject;
import org.apache.gravitino.authorization.SecurableObjects;
import org.apache.gravitino.authorization.common.RangerAuthorizationProperties;
import org.apache.gravitino.integration.test.container.HiveContainer;
import org.apache.gravitino.integration.test.container.RangerContainer;
import org.apache.gravitino.integration.test.util.GravitinoITUtils;
import org.apache.kyuubi.plugin.spark.authz.AccessControlException;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("gravitino-docker-test")
public class RangerPaimonE2EIT extends RangerBaseE2EIT {
  private static final Logger LOG = LoggerFactory.getLogger(RangerPaimonE2EIT.class);

  private static final String provider = "lakehouse-paimon";
  private static final String SQL_USE_CATALOG = "USE paimon";

  @BeforeAll
  public void startIntegrationTest() throws Exception {
    metalakeName = GravitinoITUtils.genRandomName("metalake").toLowerCase();
    // Enable Gravitino Authorization mode
    Map<String, String> configs = Maps.newHashMap();
    configs.put(Configs.ENABLE_AUTHORIZATION.getKey(), String.valueOf(true));
    configs.put(Configs.SERVICE_ADMINS.getKey(), RangerITEnv.HADOOP_USER_NAME);
    configs.put(Configs.AUTHENTICATORS.getKey(), AuthenticatorType.SIMPLE.name().toLowerCase());
    configs.put("SimpleAuthUserName", AuthConstants.ANONYMOUS_USER);
    registerCustomConfigs(configs);
    super.startIntegrationTest();

    RangerITEnv.init(RangerBaseE2EIT.metalakeName, true);
    RangerITEnv.startHiveRangerContainer();

    HIVE_METASTORE_URIS =
        String.format(
            "thrift://%s:%d",
            containerSuite.getHiveRangerContainer().getContainerIpAddress(),
            HiveContainer.HIVE_METASTORE_PORT);

    generateRangerSparkSecurityXML("authorization-ranger");

    sparkSession =
        SparkSession.builder()
            .master("local[1]")
            .appName("Ranger Paimon E2E integration test")
            .config("spark.sql.catalog.paimon", "org.apache.paimon.spark.SparkCatalog")
            .config("spark.sql.catalog.paimon.metastore", "hive")
            .config("spark.sql.catalog.paimon.uri", HIVE_METASTORE_URIS)
            .config(
                "spark.sql.catalog.paimon.warehouse",
                String.format(
                    "hdfs://%s:%d/user/hive/warehouse",
                    containerSuite.getHiveRangerContainer().getContainerIpAddress(),
                    HiveContainer.HDFS_DEFAULTFS_PORT))
            .config("spark.sql.catalog.paimon.cache-enabled", "false")
            .config(
                "spark.sql.extensions",
                "org.apache.kyuubi.plugin.spark.authz.ranger.RangerSparkExtension,"
                    + "org.apache.paimon.spark.extensions.PaimonSparkSessionExtensions")
            .enableHiveSupport()
            .getOrCreate();

    createMetalake();
    createCatalog();

    RangerITEnv.cleanup();
    metalake.addUser(System.getenv(HADOOP_USER_NAME));
  }

  @AfterAll
  public void stop() {
    cleanIT();
  }

  @Override
  protected String testUserName() {
    return System.getenv(HADOOP_USER_NAME);
  }

  @Override
  protected void reset() {
    String userName1 = System.getenv(HADOOP_USER_NAME);
    String roleName = currentFunName();
    SecurableObject securableObject =
        SecurableObjects.ofMetalake(
            metalakeName, Lists.newArrayList(Privileges.UseCatalog.allow()));
    metalake.createRole(roleName, Collections.emptyMap(), Lists.newArrayList(securableObject));
    metalake.grantRolesToUser(Lists.newArrayList(roleName), userName1);
    waitForUpdatingPolicies();
    sparkSession.sql(SQL_USE_CATALOG);
    metalake.deleteRole(roleName);
    waitForUpdatingPolicies();
  }

  @Override
  protected void checkUpdateSQLWithSelectModifyPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to update yet.
  }

  @Override
  protected void checkUpdateSQLWithSelectPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to update yet.
  }

  @Override
  protected void checkUpdateSQLWithModifyPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to update yet.
  }

  @Override
  protected void checkDeleteSQLWithSelectModifyPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to delete yet.
  }

  @Override
  protected void checkDeleteSQLWithSelectPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to delete yet.
  }

  @Override
  protected void checkDeleteSQLWithModifyPrivileges() {
    // Kyuubi Paimon Ranger plugin doesn't support to delete yet.
  }

  @Override
  protected void checkWithoutPrivileges() {
    Assertions.assertThrows(AccessControlException.class, () -> sparkSession.sql(SQL_INSERT_TABLE));
    Assertions.assertThrows(
        AccessControlException.class, () -> sparkSession.sql(SQL_SELECT_TABLE).collectAsList());
    Assertions.assertThrows(AccessControlException.class, () -> sparkSession.sql(SQL_DROP_TABLE));
    Assertions.assertThrows(AccessControlException.class, () -> sparkSession.sql(SQL_DROP_SCHEMA));
    Assertions.assertThrows(AccessControlException.class, () -> sparkSession.sql(SQL_USE_SCHEMA));
    Assertions.assertThrows(
        AccessControlException.class, () -> sparkSession.sql(SQL_CREATE_SCHEMA));
    Assertions.assertThrows(AccessControlException.class, () -> sparkSession.sql(SQL_CREATE_TABLE));
  }

  @Override
  protected void testAlterTable() {
    sparkSession.sql(SQL_ALTER_TABLE);
    sparkSession.sql(SQL_ALTER_TABLE_BACK);
  }

  @Override
  public void createCatalog() {
    Map<String, String> properties =
        ImmutableMap.of(
            "uri",
            HIVE_METASTORE_URIS,
            "catalog-backend",
            "hive",
            "warehouse",
            String.format(
                "hdfs://%s:%d/user/hive/warehouse",
                containerSuite.getHiveRangerContainer().getContainerIpAddress(),
                HiveContainer.HDFS_DEFAULTFS_PORT),
            AUTHORIZATION_PROVIDER,
            "ranger",
            RangerAuthorizationProperties.RANGER_SERVICE_TYPE,
            "HadoopSQL",
            RangerAuthorizationProperties.RANGER_SERVICE_NAME,
            RangerITEnv.RANGER_HIVE_REPO_NAME,
            RangerAuthorizationProperties.RANGER_ADMIN_URL,
            RangerITEnv.RANGER_ADMIN_URL,
            RangerAuthorizationProperties.RANGER_AUTH_TYPE,
            RangerContainer.authType,
            RangerAuthorizationProperties.RANGER_USERNAME,
            RangerContainer.rangerUserName,
            RangerAuthorizationProperties.RANGER_PASSWORD,
            RangerContainer.rangerPassword);

    metalake.createCatalog(catalogName, Catalog.Type.RELATIONAL, provider, "comment", properties);
    catalog = metalake.loadCatalog(catalogName);
    LOG.info("Catalog created: {}", catalog);
  }

  protected void checkTableAllPrivilegesExceptForCreating() {
    // - a. Succeed to insert data into the table
    sparkSession.sql(SQL_INSERT_TABLE);

    // - b. Succeed to select data from the table
    sparkSession.sql(SQL_SELECT_TABLE).collectAsList();

    // - c: Succeed to update data in the table.
    sparkSession.sql(SQL_UPDATE_TABLE);

    // - d: Succeed to delete data from the table.
    sparkSession.sql(SQL_DELETE_TABLE);

    // - e: Succeed to alter the table
    sparkSession.sql(SQL_ALTER_TABLE);

    // - f: Succeed to drop the table
    sparkSession.sql(SQL_DROP_TABLE);
  }
}
