package github.javaguide.annotation;

import github.javaguide.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.lang.annotation.*;

/**
 * 包扫描
 * @author zhp
 * @date 2022-10-27 4:32
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
@Import(CustomScannerRegistrar.class)//加载该注解前先注入自定义扫描登录器
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcScan {
    //要扫描的包路径
    String[] basePackage();
}
