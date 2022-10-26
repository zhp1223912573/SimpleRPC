package github.javaguide.spring;

import github.javaguide.annotation.RpcScan;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.provider.HelloService;
import github.javaguide.remoting.transport.socket.SocketRpcServer;

/**
 * @author zhp
 * @date 2022-10-26 14:13
 */
@RpcScan(basePackage ={"github.javaguide"})
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
