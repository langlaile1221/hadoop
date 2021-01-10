package org.apache.hadoop.ipc.zhz_ipc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.hadoop.ipc.ClientId;
import org.apache.hadoop.ipc.ProcessingDetails;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RpcServerException;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.ipc.Server.Call;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzAddRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzAddRequestProto2;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzAddResponseProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzAuthMethodResponseProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEchoRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEchoRequestProto2;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEchoResponseProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEchoResponseProto2;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEmptyRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzEmptyResponseProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzExchangeRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzExchangeResponseProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzSleepRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzSlowPingRequestProto;
import org.apache.hadoop.ipc.protobuf.ZhzTestProtos.ZhzUserResponseProto;
import org.apache.hadoop.security.SaslRpcServer.AuthMethod;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.thirdparty.protobuf.RpcController;
import org.apache.hadoop.thirdparty.protobuf.ServiceException;
import org.apache.hadoop.util.Time;
import org.junit.Assert;

public class ZhzTestRpcServicePb implements ZhzTestRpcService {

  CountDownLatch fastPingCounter = new CountDownLatch(2);
  private final Lock lock = new ReentrantLock();
  private List<Call> postponedCalls = new ArrayList<>();


  @Override
  public ZhzEmptyResponseProto ping(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    // Ensure clientId is received
    byte[] clientId = Server.getClientId();
    Assert.assertNotNull(clientId);
    Assert.assertEquals(ClientId.BYTE_LENGTH, clientId.length);
    return ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzEchoResponseProto echo(RpcController controller, ZhzEchoRequestProto request)
      throws ServiceException {
    return ZhzTestProtos.ZhzEchoResponseProto.newBuilder().setMessage(
        request.getMessage())
        .build();
  }

  @Override
  public ZhzEmptyResponseProto error(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    throw new ServiceException("error", new RpcServerException("error"));
  }

  @Override
  public ZhzEmptyResponseProto error2(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    throw new ServiceException("error", new URISyntaxException("",
        "testException"));
  }

  @Override
  public ZhzEmptyResponseProto slowPing(RpcController controller, ZhzSlowPingRequestProto request)
      throws ServiceException {
    boolean shouldSlow = request.getShouldSlow();
    if (shouldSlow) {
      try {
        fastPingCounter.await(); //slow response until two fast pings happened
      } catch (InterruptedException ignored) {
      }
    } else {
      fastPingCounter.countDown();
    }

    return ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzEchoResponseProto2 echo2(RpcController controller, ZhzEchoRequestProto2 request)
      throws ServiceException {
    return ZhzTestProtos.ZhzEchoResponseProto2.newBuilder().addAllMessage(
        request.getMessageList()).build();
  }

  @Override
  public ZhzAddResponseProto add(RpcController controller, ZhzAddRequestProto request)
      throws ServiceException {
    return ZhzTestProtos.ZhzAddResponseProto.newBuilder().setResult(
        request.getParam1() + request.getParam2()).build();
  }

  @Override
  public ZhzAddResponseProto add2(RpcController controller, ZhzAddRequestProto2 request)
      throws ServiceException {
    int sum = 0;
    for (Integer num : request.getParamsList()) {
      sum += num;
    }
    return ZhzTestProtos.ZhzAddResponseProto.newBuilder().setResult(sum).build();
  }

  @Override
  public ZhzEmptyResponseProto testServerGet(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    if (!(Server.get() instanceof RPC.Server)) {
      throw new ServiceException("Server.get() failed");
    }
    return ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzExchangeResponseProto exchange(RpcController controller,
      ZhzExchangeRequestProto request) throws ServiceException {
    Integer[] values = new Integer[request.getValuesCount()];
    for (int i = 0; i < values.length; i++) {
      values[i] = i;
    }
    return ZhzTestProtos.ZhzExchangeResponseProto.newBuilder()
        .addAllValues(Arrays.asList(values)).build();
  }

  @Override
  public ZhzEmptyResponseProto sleep(RpcController controller, ZhzSleepRequestProto request)
      throws ServiceException {
    try {
      Thread.sleep(request.getMilliSeconds());
    } catch (InterruptedException ignore) {}
    return  ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzEmptyResponseProto lockAndSleep(RpcController controller, ZhzSleepRequestProto request)
      throws ServiceException {
    ProcessingDetails details =
        Server.getCurCall().get().getProcessingDetails();
    lock.lock();
    long startNanos = Time.monotonicNowNanos();
    try {
      Thread.sleep(request.getMilliSeconds());
    } catch (InterruptedException ignore) {
      // ignore
    } finally {
      lock.unlock();
    }
    // Add some arbitrary large lock wait time since in any test scenario
    // the lock wait time will probably actually be too small to notice
    details.add(ProcessingDetails.Timing.LOCKWAIT, 10, TimeUnit.SECONDS);
    details.add(ProcessingDetails.Timing.LOCKEXCLUSIVE,
        Time.monotonicNowNanos() - startNanos, TimeUnit.NANOSECONDS);
    return  ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzAuthMethodResponseProto getAuthMethod(RpcController controller,
      ZhzEmptyRequestProto request) throws ServiceException {
    AuthMethod authMethod = null;
    try {
      authMethod = UserGroupInformation.getCurrentUser()
          .getAuthenticationMethod().getAuthMethod();
    } catch (IOException e) {
      throw new ServiceException(e);
    }

    return ZhzTestProtos.ZhzAuthMethodResponseProto.newBuilder()
        .setCode(authMethod.code)
        .setMechanismName(authMethod.getMechanismName())
        .build();
  }

  @Override
  public ZhzUserResponseProto getAuthUser(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    UserGroupInformation authUser;
    try {
      authUser = UserGroupInformation.getCurrentUser();
    } catch (IOException e) {
      throw new ServiceException(e);
    }

    return newUserResponse(authUser.getUserName());  }

  @Override
  public ZhzEchoResponseProto echoPostponed(RpcController controller, ZhzEchoRequestProto request)
      throws ServiceException {
    Server.Call call = Server.getCurCall().get();
    call.postponeResponse();
    postponedCalls.add(call);

    return ZhzTestProtos.ZhzEchoResponseProto.newBuilder().setMessage(
        request.getMessage())
        .build();
  }

  @Override
  public ZhzEmptyResponseProto sendPostponed(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    Collections.shuffle(postponedCalls);
    try {
      for (Server.Call call : postponedCalls) {
        call.sendResponse();
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    }
    postponedCalls.clear();

    return ZhzTestProtos.ZhzEmptyResponseProto.newBuilder().build();
  }

  @Override
  public ZhzUserResponseProto getCurrentUser(RpcController controller, ZhzEmptyRequestProto request)
      throws ServiceException {
    String user;
    try {
      user = UserGroupInformation.getCurrentUser().toString();
    } catch (IOException e) {
      throw new ServiceException("Failed to get current user", e);
    }

    return newUserResponse(user);
  }

  @Override
  public ZhzUserResponseProto getServerRemoteUser(RpcController controller,
      ZhzEmptyRequestProto request) throws ServiceException {
    String serverRemoteUser = Server.getRemoteUser().toString();
    return newUserResponse(serverRemoteUser);
  }

  private ZhzUserResponseProto newUserResponse(String user) {
    return ZhzTestProtos.ZhzUserResponseProto.newBuilder()
        .setUser(user)
        .build();
  }
}
