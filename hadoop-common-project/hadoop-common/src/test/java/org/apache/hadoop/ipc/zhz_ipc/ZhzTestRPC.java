/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License.  You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.hadoop.ipc.zhz_ipc;

import static org.apache.hadoop.test.MetricsAsserts.assertCounter;
import static org.apache.hadoop.test.MetricsAsserts.getMetrics;
import static org.junit.Assert.assertEquals;
import org.apache.hadoop.ipc.ProtobufRpcEngine2;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEmptyResponseProto;
import org.apache.hadoop.thirdparty.protobuf.ServiceException;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos;
import org.apache.hadoop.ipc.protobuf.ZhzTestRpcServiceProtos;
import org.apache.hadoop.metrics2.MetricsRecordBuilder;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.thirdparty.protobuf.BlockingService;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unit tests for RPC. */
@SuppressWarnings("deprecation")
public class ZhzTestRPC {

  public static final Logger LOG = LoggerFactory.getLogger(ZhzTestRPC.class);
  protected static Configuration conf;
  protected final static String ADDRESS = "0.0.0.0";
  protected final static int PORT = 0;
  protected static InetSocketAddress addr;

  @Before
  public void setup() {
    setupConf();
  }

  int datasize = 1024 * 100;
  int numThreads = 50;

  protected void setupConf() {
    conf = new Configuration();
    // Set RPC engine to protobuf RPC engine
    RPC.setProtocolEngine(conf, ZhzTestRpcService.class, ProtobufRpcEngine2.class);
    UserGroupInformation.setConfiguration(conf);
  }

  @Test
  public void testConfRpc() throws Exception {
    Server server = newServerBuilder(conf)
        .setNumHandlers(1).setVerbose(false).build();

    // Just one handler
    int confQ = conf.getInt(
        CommonConfigurationKeys.IPC_SERVER_HANDLER_QUEUE_SIZE_KEY,
        CommonConfigurationKeys.IPC_SERVER_HANDLER_QUEUE_SIZE_DEFAULT);
    assertEquals(confQ, server.getMaxQueueSize());

    server.start();
    addr = NetUtils.getConnectAddress(server);
    //客户端
    ZhzTestRpcService proxy = null;
    ZhzTestProtos.ZhzEmptyRequestProto emptyRequestProto = ZhzTestProtos.ZhzEmptyRequestProto
        .newBuilder().build();
    try {
      proxy = getClient(addr, conf);
      
      ZhzEmptyResponseProto responseProto = proxy.ping(null, emptyRequestProto);
      System.out.println(responseProto);
    } catch (ServiceException e) {
      e.printStackTrace();
    } finally {
      MetricsRecordBuilder rb = getMetrics(server.getRpcMetrics().name());

      //since we don't have authentication turned ON, we should see 
      // 0 for the authentication successes and 0 for failure
      assertCounter("RpcAuthenticationFailures", 0L, rb);
      assertCounter("RpcAuthenticationSuccesses", 0L, rb);

      stop(server, proxy);
    }
  }

  protected static RPC.Builder newServerBuilder(
      Configuration serverConf) throws IOException {
    // Create server side implementation
    ZhzTestRpcServicePb serverImpl = new ZhzTestRpcServicePb();
    BlockingService service = ZhzTestRpcServiceProtos.ZhzTestProtobufRpcProto
        .newReflectiveBlockingService(serverImpl);

    // Get RPC server for server side implementation
    RPC.Builder builder = new RPC.Builder(serverConf)
        .setProtocol(ZhzTestRpcService.class)
        .setInstance(service).setBindAddress(ADDRESS).setPort(PORT);

    return builder;
  }

  protected static ZhzTestRpcService getClient(InetSocketAddress serverAddr,
      Configuration clientConf)
      throws ServiceException {
    try {
      return RPC.getProxy(ZhzTestRpcService.class, 0, serverAddr, clientConf);
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  protected static void stop(Server server, ZhzTestRpcService proxy) {
    if (proxy != null) {
      try {
        RPC.stopProxy(proxy);
      } catch (Exception ignored) {
      }
    }

    if (server != null) {
      try {
        server.stop();
      } catch (Exception ignored) {
      }
    }
  }


}
