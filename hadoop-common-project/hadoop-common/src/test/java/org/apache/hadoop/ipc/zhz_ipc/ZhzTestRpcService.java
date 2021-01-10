package org.apache.hadoop.ipc.zhz_ipc;

import static org.apache.hadoop.ipc.zhz_ipc.ZhzTestRpcConstanst.SERVER_PRINCIPAL_KEY;

import org.apache.hadoop.ipc.ProtocolInfo;
import org.apache.hadoop.ipc.TestRpcBase.TestTokenSelector;
import org.apache.hadoop.ipc.protobuf.ZhzTestRpcServiceProtos;
import org.apache.hadoop.security.KerberosInfo;
import org.apache.hadoop.security.token.TokenInfo;

/**
 * 这个主要增加一些备注
 */
@KerberosInfo(serverPrincipal = SERVER_PRINCIPAL_KEY)
@TokenInfo(TestTokenSelector.class)
@ProtocolInfo(protocolName = "org.apache.hadoop.ipc.TestRpcBase$TestRpcService",
    protocolVersion = 1)
public interface ZhzTestRpcService
    extends ZhzTestRpcServiceProtos.ZhzTestProtobufRpcProto.BlockingInterface {
}