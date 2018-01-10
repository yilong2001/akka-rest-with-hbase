/*
 *
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

package com.example.akka.hbase.conn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.rest.RowSpec;
import org.apache.hadoop.hbase.rest.model.CellModel;
import org.apache.hadoop.hbase.rest.model.CellSetModel;
import org.apache.hadoop.hbase.rest.model.RowModel;
import org.apache.hadoop.hbase.util.Bytes;

public class RowResource {
  private static final Log LOG = LogFactory.getLog(RowResource.class);

  public static CellSetModel get(final Table table,
                                 final RowSpec rowspec,
                                 final boolean nochache) {
    long start = System.currentTimeMillis();
    try {
      RowResultGenerator generator = new RowResultGenerator(table, rowspec, null, !nochache);
      if (!generator.hasNext()) {
        return null;
      }

      int count = 0;
      CellSetModel model = new CellSetModel();
      Cell value = generator.next();
      byte[] rowKey = CellUtil.cloneRow(value);
      RowModel rowModel = new RowModel(rowKey);
      do {
        if (!Bytes.equals(CellUtil.cloneRow(value), rowKey)) {
          model.addRow(rowModel);
          rowKey = CellUtil.cloneRow(value);
          rowModel = new RowModel(rowKey);
        }
        rowModel.addCell(new CellModel(CellUtil.cloneFamily(value), CellUtil.cloneQualifier(value),
          value.getTimestamp(), CellUtil.cloneValue(value)));
        if (++count > rowspec.getMaxValues()) {
          break;
        }
        value = generator.next();
      } while (value != null);
      model.addRow(rowModel);
      return model;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
