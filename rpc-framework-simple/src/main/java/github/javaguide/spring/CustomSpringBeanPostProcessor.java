package github.javaguide.spring;

import github.javaguide.annotation.RpcReference;
import github.javaguide.annotation.RpcService;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.ZkServiceProviderImpl;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author zhp
 * @date 2022-10-27 4:47
 * 自定义后置处理器
 *
 * 在bean实例化前后进行拦截，实现目标操作。
 * 实例化前：读取@RpcService的相关配置信息，并将注解标记的服务具体实现类发布到注册中心。
 * 实例化后：读取@RpcReference的相关配置信息，并通过RpcClientProxy创建该服务的代理实现类，将代理对象注入类中被该注解标记的成员变量。
 */
@Slf4j
@Component
public class CustomSpringBeanPostProcessor implements BeanPostProcessor {

    //rpc请求发送客户端 （netty实现和socket实现）
    private final RpcRequestTransport rpcClient;
    //服务提供器（发布服务）
    private final ServiceProvider serviceProvider;

    public CustomSpringBeanPostProcessor() {
        rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("socket");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //读取bean信息，找到被@RpcService标记的实例
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}] 被注解 [{}] 标记", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //得到@RpcService注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            //读取@RpcService中的配置信息，发布服务
            serviceProvider.publishService(
                    new RpcServiceConfig().builder()
                            .version(rpcService.version())
                            .group(rpcService.group())
                            .service(bean)
                            .build());
        }
        return bean;
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        //获取bean实例内部成员变量
        Field[] declaredFields = targetClass.getDeclaredFields();
        //遍历所有成员变量
        for(Field declaredField : declaredFields){
            //得到被@RpcReference标记的成员
            RpcReference reference = declaredField.getAnnotation(RpcReference.class);
            if(reference!=null){
                //读取注解配置信息,创建服务配置信息
                RpcServiceConfig rpcServiceConfig = new RpcServiceConfig().builder()
                        .group(reference.group()).version(reference.version()).build();
                //通过RpcClientProxy创建代理对象
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient,rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                //设置成员变量可设置
                declaredField.setAccessible(true);
                //注入到bean内部成员变量
                try {
                    declaredField.set(bean, proxy);
                    log.info("实例 [{}] 内部注入成员变量 [{}]",bean.getClass().getName(),proxy.getClass().getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

}
