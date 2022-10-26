package github.javaguide.transport.socket;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.provider.HelloService;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.transport.socket.SocketRpcClient;
import github.javaguide.remoting.transport.socket.SocketRpcServer;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.UUID;

/**
 * @author zhp
 * @date 2022-10-26 14:13
 */
public class tesSocketRpcServer {

    public static void main(String[] args) {
        tesSocketRpcServer a = new tesSocketRpcServer();
        a.testSocketRpcServer();
    }


    public void testSocketRpcServer(){
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        HelloService helloService = new HelloService();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setVersion("1.0");
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
