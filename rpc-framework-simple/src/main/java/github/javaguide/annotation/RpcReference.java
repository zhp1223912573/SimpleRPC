package github.javaguide.annotation;

import java.lang.annotation.*;

/**
 * @author zhp
 * @date 2022-10-27 4:40
 * 自动注入服务调用
 *
 * 同样，在spring扫描包内组件时，会在后置处理器的postProcessAfterInitialization中，
 * 通过RpcClientProxy创建被注解标记的成员属性，并将该代理类注入该引用内。
 */
@Documented
@Target({ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    //服务版本
     String version() default "";

    //服务组（同一服务接口可能存在多个具体实现）
     String group() default "";
}
