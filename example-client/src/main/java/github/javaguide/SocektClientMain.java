package github.javaguide;

import github.javaguide.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zhp
 * @date 2022-10-27 5:12
 */
@RpcScan(basePackage = {"github.javaguide"})
public class SocektClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext(SocektClientMain.class);
        HelloController helloController = (HelloController) annotationConfigApplicationContext.getBean("helloController");
        helloController.test();
    }
}
