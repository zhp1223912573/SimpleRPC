package github.javaguide.annotation;

import java.lang.annotation.*;

/**
 * @author zhp
 * @date 2022-10-27 4:35
 * 标记服务具体实现，表明版本和group(表示同一接口的不同类实现）
 * 被标记的实现类会如果放在指定的路径basepackge下，会被spring扫描到，同时将该注解的服务注册到注册中心。
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    //服务版本
     String version() default "";
    //服务组（同一服务接口可能存在多个具体实现）
     String group() default "";
}
