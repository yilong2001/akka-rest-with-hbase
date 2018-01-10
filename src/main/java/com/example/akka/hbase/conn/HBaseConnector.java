/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.akka.hbase.conn;

import com.example.akka.utils.KbrUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ParseFilter;
import org.apache.hadoop.security.UserGroupInformation;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HBaseConnector implements Constants {
  private static final Log LOG = LogFactory.getLog(HBaseConnector.class);

  private final Map<String, Connection>
          connections = new ConcurrentHashMap<String, Connection>();

  private final Configuration conf;
  private final String kerberosJassPath;
  private final String kerberosKrb5Path;
  private final String kerberosJassKey;

  public HBaseConnector(final Configuration conf,
                        String kerberosJassPath,
                        String kerberosKrb5Path,
                        String kerberosJassKey) throws IOException {
    this.kerberosJassKey = kerberosJassKey;
    this.kerberosJassPath = kerberosJassPath;
    this.kerberosKrb5Path = kerberosKrb5Path;

    this.conf = conf;
    this.conf.set("hadoop.security.authentication", "kerberos");
    UserGroupInformation.setConfiguration(conf);
    registerCustomFilter(conf);

    LOG.info("HBaseConnector construct function end");
  }

  public Table getTable(String user, String tableName) throws IOException {
    long start = System.currentTimeMillis();

    Connection conn = null;
    try {
      conn = connections.get(user);
      if (conn == null || conn.isClosed() || conn.isAborted()) {
        LOG.info("user="+user+"; table="+tableName+", connect is null, firsty create connection");
        if (kerberosJassKey != null) {
          conn = getConnectionWithoutKbr(user);
        } else {
          conn = getConnectionWithoutKbr(user);
        }
        connections.put(user, conn);
      }
    } finally {
    }

    Table table = conn.getTable(TableName.valueOf(tableName));
    return table;
  }

//  public Table getTableWithoutKbr(String user, String tableName) throws IOException {
//    long start = System.currentTimeMillis();
//
//    Connection conn = null;
//    try {
//      conn = connections.get(user);
//      if (conn == null || conn.isClosed() || conn.isAborted()) {
//        conn = getConnection(user);
//        connections.put(user, conn);
//      }
//    } finally {
//    }
//
//    Table table = conn.getTable(TableName.valueOf(tableName));
//    return table;
//  }
//
//  public Table getTableWithKbr(LoginContext context, String user, String tableName) throws IOException {
//    long start = System.currentTimeMillis();
////return null;
//    Connection conn = null;
//    try {
//      conn = connections.get(user);
//      if (conn == null || conn.isClosed() || conn.isAborted()) {
//        conn = getConnection(context, user);
//        connections.put(user, conn);
//      }
//    } finally {
//    }
//
//    Table table = conn.getTable(TableName.valueOf(tableName));
//    return table;
//  }

  public Configuration getConfiguration() {
    return conf;
  }

  public Connection getConnectionWithoutKbr(String user) throws IOException {
    return ConnectionFactory.createConnection(conf);
  }

  Connection getConnectionWithKbr(String user) throws IOException {
    KbrUtil.kinit(this.kerberosJassPath, this.kerberosKrb5Path, this.kerberosJassKey);
    Subject subject = KbrUtil.getContext().getSubject();

    LOG.debug("getConnection : user = " + user);

    UserGroupInformation ugiFromSubject = UserGroupInformation.getUGIFromSubject(subject);
    UserGroupInformation proxyUser = UserGroupInformation.createProxyUser(user, ugiFromSubject);
    try {
      return proxyUser.doAs(new PrivilegedExceptionAction<Connection>() {
        @Override
        public Connection run() throws Exception {
          Connection conn = ConnectionFactory.createConnection(conf);
          return conn;
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("get connect error" + e.getMessage(), e);
      return null;
    }
  }

  /**
   * Shutdown any services that need to stop
   */
  public void shutdown() {
    for (String user : connections.keySet()) {
      Connection conn = connections.remove(user);
      if (conn != null) {
        try {
          conn.close();
        } catch (IOException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }

  private void registerCustomFilter(Configuration conf) {
    String[] filterList = conf.getStrings(Constants.CUSTOM_FILTERS);
    if (filterList != null) {
      for (String filterClass : filterList) {
        String[] filterPart = filterClass.split(":");
        if (filterPart.length != 2) {
          LOG.warn(
            "Invalid filter specification " + filterClass + " - skipping");
        } else {
          ParseFilter.registerFilter(filterPart[0], filterPart[1]);
        }
      }
    }
  }
}
