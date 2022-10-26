package github.javaguide.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 *
 * @author zhp
 * @date 2022-10-27 4:49
 * 自定义包扫描
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {

    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        //添加要扫描的注解 使@RpcServer注解标记的类服务能被加载（在后续初始化bean时实现注册的流程）
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    public int scan(String... basePackages){
        return super.scan(basePackages);
    }
}
