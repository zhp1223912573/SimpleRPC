package github.javaguide;

import github.javaguide.annotation.RpcScan;
import github.javaguide.remoting.transport.socket.SocketRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zhp
 * @date 2022-10-27 5:12
 */
@RpcScan(basePackage = {"github.javaguide"})
public class SocetClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext(SocetClientMain.class);
        HelloController helloController = (HelloController) annotationConfigApplicationContext.getBean("helloController");
        helloController.test();
    }
}
