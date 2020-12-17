package org.apache.hadoop.hdfs.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BlockTest {

  public static void main(String[] args) throws IOException {
    Block block = new Block(7806259420524417791L, 39447755L, 56736651L);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(bout);
    block.write(dout);
    dout.close();
    System.out.println("...................");
    System.out.println(Arrays.toString(bout.toByteArray()));
    System.out.println(bout.size());
  }
}
