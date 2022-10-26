package github;

import github.javaguide.annotation.RpcScan;
import github.javaguide.remoting.transport.socket.SocketRpcClient;
import github.javaguide.remoting.transport.socket.SocketRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zhp
 * @date 2022-10-27 4:45
 */
@RpcScan(basePackage = {"github.javaguide"})
public class SocketServerMain {
    public static void main(String[] args) {
        //通过注解启动
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SocketServerMain.class);
        //获取socketRpcServer
        SocketRpcServer socketRpcServer =(SocketRpcServer) annotationConfigApplicationContext.getBean("socketRpcServer");
        socketRpcServer.start();
    }
}
