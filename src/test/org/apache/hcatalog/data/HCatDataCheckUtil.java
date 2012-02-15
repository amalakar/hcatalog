/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hcatalog.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.CommandNeedRetryException;
import org.apache.hadoop.hive.ql.Driver;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hcatalog.MiniCluster;
import org.apache.hcatalog.common.HCatUtil;
import org.apache.hcatalog.mapreduce.HCatOutputFormat;

/**
 * Helper class for Other Data Testers
 */
public class HCatDataCheckUtil {

  public static Driver instantiateDriver(MiniCluster cluster) {
    HiveConf hiveConf = new HiveConf(HCatDataCheckUtil.class);
    for (Entry e : cluster.getProperties().entrySet()){
      hiveConf.set(e.getKey().toString(), e.getValue().toString());
    }
    hiveConf.set(HiveConf.ConfVars.PREEXECHOOKS.varname, "");
    hiveConf.set(HiveConf.ConfVars.POSTEXECHOOKS.varname, "");
    hiveConf.set(HiveConf.ConfVars.HIVE_SUPPORT_CONCURRENCY.varname, "false");
    
    Log logger = LogFactory.getLog(HCatOutputFormat.class);
    HCatUtil.logHiveConf(logger , hiveConf);
    
    Driver driver = new Driver(hiveConf);
    SessionState.start(new CliSessionState(hiveConf));
    return driver;
  }

  public static void generateDataFile(MiniCluster cluster, String fileName) throws IOException {
    MiniCluster.deleteFile(cluster, fileName);
    String[] input = new String[50];
    for(int i = 0; i < 50; i++) {
      input[i] = (i % 5) + "\t" + i  + "\t" + "_S" + i + "S_";
    }
    MiniCluster.createInputFile(cluster, fileName, input);
  }

  public static void createTable(Driver driver, String tableName, String createTableArgs)
      throws CommandNeedRetryException, IOException {
    String createTable = "create table " + tableName + createTableArgs;
    int retCode = driver.run(createTable).getResponseCode();
    if(retCode != 0) {
      throw new IOException("Failed to create table. ["+createTable+"], return code from hive driver : ["+retCode+"]");
    }
  }

  public static void dropTable(Driver driver, String tablename) throws IOException, CommandNeedRetryException{
    driver.run("drop table if exists "+tablename);
  }

  public static ArrayList<String> formattedRun(Driver driver, String name, String selectCmd)
      throws CommandNeedRetryException, IOException {
    driver.run(selectCmd);
    ArrayList<String> src_values = new ArrayList<String>();
    driver.getResults(src_values);
    for (String s : src_values){
      System.out.println(name+":"+s);
    }
    return src_values;
  }

}