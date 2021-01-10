package org.apache.hadoop.ipc.zhz_ipc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.thirdparty.protobuf.ServiceException;
import java.util.Arrays;
import org.apache.hadoop.ipc.TestRpcBase.TestRpcService;
import org.apache.hadoop.ipc.protobuf.TestProtos;

public class Transactions implements Runnable {
  int datasize;
  TestRpcService proxy;

  Transactions(TestRpcService proxy, int datasize) {
    this.proxy = proxy;
    this.datasize = datasize;
  }

  // do two RPC that transfers data.
  @Override
  public void run() {
    Integer[] indata = new Integer[datasize];
    Arrays.fill(indata, 123);
    TestProtos.ExchangeRequestProto exchangeRequest =
        TestProtos.ExchangeRequestProto.newBuilder().addAllValues(
            Arrays.asList(indata)).build();
    Integer[] outdata = null;
    TestProtos.ExchangeResponseProto exchangeResponse;

    TestProtos.AddRequestProto addRequest =
        TestProtos.AddRequestProto.newBuilder().setParam1(1)
            .setParam2(2).build();
    TestProtos.AddResponseProto addResponse;

    int val = 0;
    try {
      exchangeResponse = proxy.exchange(null, exchangeRequest);
      outdata = new Integer[exchangeResponse.getValuesCount()];
      outdata = exchangeResponse.getValuesList().toArray(outdata);
      addResponse = proxy.add(null, addRequest);
      val = addResponse.getResult();
    } catch (ServiceException e) {
      assertTrue("Exception from RPC exchange() "  + e, false);
    }
    assertEquals(indata.length, outdata.length);
    assertEquals(3, val);
    for (int i = 0; i < outdata.length; i++) {
      assertEquals(outdata[i].intValue(), i);
    }
  }
}
