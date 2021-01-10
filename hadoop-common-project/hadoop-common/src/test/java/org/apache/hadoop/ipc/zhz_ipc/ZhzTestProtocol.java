package org.apache.hadoop.ipc.zhz_ipc;

import java.io.IOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.ipc.VersionedProtocol;

public interface ZhzTestProtocol extends VersionedProtocol {
  long versionID = 1L;

  void ping() throws IOException;
  void sleep(long delay) throws IOException, InterruptedException;
  String echo(String value) throws IOException;
  String[] echo(String[] value) throws IOException;
  Writable echo(Writable value) throws IOException;
  int add(int v1, int v2) throws IOException;
  int add(int[] values) throws IOException;
  int error() throws IOException;
}