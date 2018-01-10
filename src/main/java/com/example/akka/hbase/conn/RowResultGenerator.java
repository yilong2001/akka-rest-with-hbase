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
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.NeedUnmanagedConnectionException;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.rest.RowSpec;
import org.apache.hadoop.util.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RowResultGenerator {
  private static final Log LOG = LogFactory.getLog(RowResultGenerator.class);

  private Iterator<Cell> valuesI;
  private Cell cache;

  public RowResultGenerator(final Table table,
                            final RowSpec rowspec,
                            final Filter filter, final boolean cacheBlocks)
      throws IllegalArgumentException, IOException {
    try {
      Get get = new Get(rowspec.getRow());
      if (rowspec.hasColumns()) {
        for (byte[] col : rowspec.getColumns()) {
          byte[][] split = KeyValue.parseColumn(col);
          if (split.length == 1) {
            get.addFamily(split[0]);
          } else if (split.length == 2) {
            get.addColumn(split[0], split[1]);
          } else {
            throw new IllegalArgumentException("Invalid column specifier.");
          }
        }
      }
      get.setTimeRange(rowspec.getStartTime(), rowspec.getEndTime());
      get.setMaxVersions(rowspec.getMaxVersions());
      if (filter != null) {
        get.setFilter(filter);
      }
      get.setCacheBlocks(cacheBlocks);
      Result result = table.get(get);
      if (result != null && !result.isEmpty()) {
        valuesI = result.listCells().iterator();
      }
    } catch (DoNotRetryIOException e1) {
      LOG.warn(StringUtils.stringifyException(e1));
    } catch (NeedUnmanagedConnectionException e2) {
      LOG.warn(StringUtils.stringifyException(e2));
    } finally {
      table.close();
    }
  }

  public void close() {
  }

  public boolean hasNext() {
    if (cache != null) {
      return true;
    }
    if (valuesI == null) {
      return false;
    }
    return valuesI.hasNext();
  }

  public Cell next() {
    if (cache != null) {
      Cell kv = cache;
      cache = null;
      return kv;
    }
    if (valuesI == null) {
      return null;
    }
    try {
      return valuesI.next();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  public void putBack(Cell kv) {
    this.cache = kv;
  }

  public void remove() {
    throw new UnsupportedOperationException("remove not supported");
  }
}
