/*
 * arcus-java-client : Arcus Java client
 * Copyright 2010-2014 NAVER Corp.
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
package net.spy.memcached.collection;

import java.util.List;
import java.util.Map;

import net.spy.memcached.util.BTreeUtil;
import net.spy.memcached.collection.SMGetMode;

public class BTreeSMGetWithLongTypeBkey<T> implements BTreeSMGet<T> {

  private static final String command = "bop smget";

  protected String str;

  protected List<String> keyList;
  private String keySeparator;
  private String spaceSeparatedKeys;

  protected int lenKeys;

  protected String range;
  protected int count;
  protected SMGetMode smgetMode;
  protected boolean unique;
  protected Map<Integer, T> map;

  protected boolean reverse;

  public String key;
  public int flag;
  public long subkey;
  public int dataLength;

  public byte[] eflag = null;

  private ElementFlagFilter eFlagFilter;

  public BTreeSMGetWithLongTypeBkey(List<String> keyList, long from, long to,
                                    ElementFlagFilter eFlagFilter, int count, SMGetMode smgetMode) {
    this.keyList = keyList;

    this.range = String.valueOf(from)
            + ((to > -1) ? ".." + String.valueOf(to) : "");

    this.eFlagFilter = eFlagFilter;
    this.count = count;
    this.smgetMode = smgetMode;
    this.reverse = (from > to);
  }

  public void setKeySeparator(String keySeparator) {
    this.keySeparator = keySeparator;
  }

  public String getSpaceSeparatedKeys() {
    if (spaceSeparatedKeys != null) {
      return spaceSeparatedKeys;
    }

    StringBuilder sb = new StringBuilder();
    int numkeys = keyList.size();
    for (int i = 0; i < numkeys; i++) {
      sb.append(keyList.get(i));
      if ((i + 1) < numkeys) {
        sb.append(keySeparator);
      }
    }
    spaceSeparatedKeys = sb.toString();
    return spaceSeparatedKeys;
  }

  public String getRepresentKey() {
    if (keyList == null || keyList.isEmpty()) {
      throw new IllegalStateException("Key list is empty.");
    }
    return keyList.get(0);
  }

  public List<String> getKeyList() {
    return keyList;
  }

  public String stringify() {
    if (str != null)
      return str;

    StringBuilder b = new StringBuilder();

    b.append(getSpaceSeparatedKeys().length());
    b.append(" ").append(keyList.size());
    b.append(" ").append(range);

    if (eFlagFilter != null)
      b.append(" ").append(eFlagFilter.toString());

    b.append(" ").append(count);

    b.append(" ").append(smgetMode.getMode());

    str = b.toString();
    return str;
  }

  public String getCommand() {
    return command;
  }

  public boolean headerReady(int spaceCount) {
    return headerCount == spaceCount;
  }

  public String getKey() {
    return key;
  }

  public int getFlag() {
    return flag;
  }

  public Long getSubkey() {
    return subkey;
  }

  public int getDataLength() {
    return dataLength;
  }

  public boolean isReverse() {
    return reverse;
  }

  public boolean hasEflag() {
    return eflag != null;
  }

  public void decodeItemHeader(String itemHeader) {
    String[] splited = itemHeader.split(" ");

    /*
    with flag
      VALUE 1
      SMGetTest31 0 1 0x45464C4147 6 VALUE1
      MISSED_KEYS 0
      END

    without flag
      VALUE 1
      SMGetTest31 0 1 6 VALUE1
      MISSED_KEYS 0
      END
     */
    this.key = splited[0];
    this.flag = Integer.parseInt(splited[1]);
    this.subkey = Long.parseLong(splited[2]);

    if (splited[3].startsWith("0x")) {
      this.eflag = BTreeUtil.hexStringToByteArrays(splited[3].substring(2));
      this.dataLength = Integer.parseInt(splited[4]);
    } else {
      this.eflag = null;
      this.dataLength = Integer.parseInt(splited[3]);
    }
  }
}