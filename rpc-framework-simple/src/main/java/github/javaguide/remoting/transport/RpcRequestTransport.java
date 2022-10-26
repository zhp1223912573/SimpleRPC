package github.javaguide.remoting.transport;

import github.javaguide.extension.SPI;
import github.javaguide.remoting.dto.RpcRequest;

/**
 * 发送rpc请求
 * @author zhp
 * @date 2022-10-26 12:45
 */
@SPI
public interface RpcRequestTransport {
    /**
     * 发送rpc请求到服务器，并接受返回结果
     * @param rpcRequest
     * @return 请求结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
