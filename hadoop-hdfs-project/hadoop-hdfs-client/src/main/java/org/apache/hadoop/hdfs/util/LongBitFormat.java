/**
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
package org.apache.hadoop.hdfs.util;

import java.io.Serializable;


/**
 * Bit format in a long.
 */
public class LongBitFormat implements Serializable {
  private static final long serialVersionUID = 1L;

  public interface Enum {
    int getLength();
  }

  private final String NAME;
  /** Bit offset */
  private final int OFFSET;
  /** Bit length */
  private final int LENGTH;
  /** Minimum value */
  private final long MIN;
  /** Maximum value */
  private final long MAX;
  /** Bit mask */
  private final long MASK;

  public LongBitFormat(String name, LongBitFormat previous, int length,
                       long min) {
    NAME = name;
    OFFSET = previous == null? 0: previous.OFFSET + previous.LENGTH;
    LENGTH = length;
    MIN = min;
    MAX = ((-1L) >>> (64 - LENGTH));
    MASK = MAX << OFFSET;
  }

  /** Retrieve the value from the record. */
  public long retrieve(long record) {
    return (record & MASK) >>> OFFSET;
  }

  /** Combine the value to the record. */
  public long combine(long value, long record) {
    if (value < MIN) {
      throw new IllegalArgumentException(
          "Illagal value: " + NAME + " = " + value + " < MIN = " + MIN);
    }
    if (value > MAX) {
      throw new IllegalArgumentException(
          "Illagal value: " + NAME + " = " + value + " > MAX = " + MAX);
    }
    return (record & ~MASK) | (value << OFFSET);
  }

  public long getMin() {
    return MIN;
  }

  public int getLength() {
    return LENGTH;
  }
}
class LongFormatTest {

  static int ID_BIT_LENGTH = 4;
  static enum HeaderFormat {
    PREFERRED_BLOCK_SIZE(null, 48, 1),
    REPLICATION(PREFERRED_BLOCK_SIZE.BITS, 12, 1),
    STORAGE_POLICY_ID(REPLICATION.BITS, ID_BIT_LENGTH,
        0);

    private final LongBitFormat BITS;

    private HeaderFormat(LongBitFormat previous, int length, long min) {
      BITS = new LongBitFormat(name(), previous, length, min);
    }

    static short getReplication(long header) {
      return (short)REPLICATION.BITS.retrieve(header);
    }

    static long getPreferredBlockSize(long header) {
      return PREFERRED_BLOCK_SIZE.BITS.retrieve(header);
    }

    static byte getStoragePolicyID(long header) {
      return (byte)STORAGE_POLICY_ID.BITS.retrieve(header);
    }

    static long toLong(long preferredBlockSize, short replication,
        byte storagePolicyID) {
      long h = 0;
      h = PREFERRED_BLOCK_SIZE.BITS.combine(preferredBlockSize, h);
      h = REPLICATION.BITS.combine(replication, h);
      h = STORAGE_POLICY_ID.BITS.combine(storagePolicyID, h);
      return h;
    }
  }
  public static void main(String[] args) {
    long blockSize = 512;
    System.out.println("block size:         " + HeaderFormat.getPreferredBlockSize(blockSize));
    long replication  = 3L  << 48;

    System.out.println("replication:        " + HeaderFormat.getReplication(replication));
    long storagePolicyID = 2L << 60;
    System.out.println("storagePolicyID:    " + HeaderFormat.getStoragePolicyID(storagePolicyID));
    System.out.println("added value:        "+ (blockSize + replication  +storagePolicyID));
    System.out.println("get combined value :" + HeaderFormat.toLong(512, (short)3 , (byte)2));

  }
}
