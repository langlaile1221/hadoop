package org.apache.hadoop.ipc.zhz_ipc;

import java.io.IOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.hadoop.ipc.TestRPC.TestProtocol;

public class ZhzTestImpl implements ZhzTestProtocol {
  int fastPingCounter = 0;

  @Override
  public long getProtocolVersion(String protocol, long clientVersion) {
    return TestProtocol.versionID;
  }

  @Override
  public ProtocolSignature getProtocolSignature(String protocol, long clientVersion,
      int hashcode) {
    return new ProtocolSignature(TestProtocol.versionID, null);
  }

  @Override
  public void ping() {}

  @Override
  public void sleep(long delay) throws InterruptedException {
    Thread.sleep(delay);
  }

  @Override
  public String echo(String value) throws IOException { return value; }

  @Override
  public String[] echo(String[] values) throws IOException { return values; }

  @Override
  public Writable echo(Writable writable) {
    return writable;
  }
  @Override
  public int add(int v1, int v2) {
    return v1 + v2;
  }

  @Override
  public int add(int[] values) {
    int sum = 0;
    for (int i = 0; i < values.length; i++) {
      sum += values[i];
    }
    return sum;
  }

  @Override
  public int error() throws IOException {
    throw new IOException("bobo");
  }
}
