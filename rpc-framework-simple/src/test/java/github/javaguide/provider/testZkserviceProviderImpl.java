package github.javaguide.provider;

import github.javaguide.config.RpcServiceConfig;
import org.junit.jupiter.api.Test;

/**
 * @author zhp
 * @date 2022-10-26 13:06
 */
public class testZkserviceProviderImpl {
    @Test
    public void test(){
        HelloService helloService = new HelloService();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setVersion("4.0");
        rpcServiceConfig.setGroup("test");
        rpcServiceConfig.setService(helloService);
        ZkServiceProviderImpl zkServiceProvider  = new ZkServiceProviderImpl();
        //测试服务发布
        zkServiceProvider.publishService(rpcServiceConfig);
        //测试服务获取
        Object service = zkServiceProvider.getService(rpcServiceConfig.getRpcServiceName());
        HelloService service1 = (HelloService) service;
        service1.doSomething("good");

    }
}
