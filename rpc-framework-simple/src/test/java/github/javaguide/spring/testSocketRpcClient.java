package github.javaguide.spring;

import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.socket.SocketRpcClient;

import java.util.UUID;

/**
 * @author zhp
 * @date 2022-10-26 14:29
 */
public class testSocketRpcClient {

    public static void main(String[] args) {
        testSocketRpcClient a = new testSocketRpcClient();
        a.testSocketRpcClient();
    }

    public void testSocketRpcClient(){
        SocketRpcClient socketRpcClient = new SocketRpcClient();
        RpcRequest rpcRequest = new RpcRequest().builder()
                .requestId(UUID.randomUUID().toString().substring(16))
                .interfaceName("github.javaguide.provider.service")
                .methodName("doSomething")
                .parameters(new Object[]{"我是逆蝶"})
                .paramTypes(new Class<?>[]{String.class})
                .version("1.0")
                .group("test")
                .build();
        RpcResponse result =(RpcResponse) socketRpcClient.sendRpcRequest(rpcRequest);
        System.out.println(rpcRequest.getRequestId());
        System.out.println(result.getRequestId());
        System.out.println(result.getCode());
        System.out.println(result.getMessage());
        System.out.println(result.getData());
    }
}
