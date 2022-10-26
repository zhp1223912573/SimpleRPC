package github.javaguide.proxy;

import github.javaguide.provider.HelloService;
import github.javaguide.provider.service;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.socket.SocketRpcClient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author zhp
 * @date 2022-10-26 14:48
 */
public class testRpcClientProxy {
    @Test
    public void test(){
        SocketRpcClient socketRpcClient = new SocketRpcClient();
//        RpcRequest rpcRequest = new RpcRequest().builder()
//                .version("1.0")
//                .group("test")
//                .build();
        RpcClientProxy proxy = new RpcClientProxy(socketRpcClient);
        service proxy1 = proxy.getProxy(service.class);

        String data = proxy1.doSomething("测试testRpcClientProxy");

        System.out.println(data);

    }
}
