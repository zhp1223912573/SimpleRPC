package github.javaguide.spring;

import github.javaguide.annotation.RpcScan;
import github.javaguide.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author zhp
 * @date 2022-10-27 4:34
 *
 * 扫描指定注解所在路径的包，将包内包含我们需要的组件加入Spring，在后续进行bean的初始化。
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    //默认扫描的包路径，会到该路径下查找是否有需要加载的组件
    private static final String SPRING_BEAN_BASE_PACKAGE = "github.javaguide";
    //读取@RpcScan的"basePackage"属性，得到要扫描的关于RPC的组件路径
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private  ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 注册要扫描的注解名称的自定义扫描器
     * @param annotationMetadata
     * @param beanDefinitionRegistry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //获取注解元数据中@RpcScan中的注解相关属性值 得到要扫描的与RPC服务相关的具体实现类的包路径
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        //
        String[] rpcScanBasePackages = new String[0];
        //获取@RpcScan扫描的包路径
        if (rpcScanAnnotationAttributes != null) {
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        //不存在包路径，则扫描注解扫在的包
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }

        //开始创建对应注解的包扫描器
        //注册@RpcService注解的扫描器
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
        //注册@Component注解的扫描器
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);

        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }

        //开始扫描
        int springBeanScanCount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量[{}]", springBeanScanCount);
        int rpcScanCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量[{}]", rpcScanCount);
    }
}
