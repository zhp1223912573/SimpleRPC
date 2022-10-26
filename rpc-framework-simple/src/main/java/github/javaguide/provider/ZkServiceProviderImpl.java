package github.javaguide.provider;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhp
 * @date 2022-10-26 12:47
 * 服务提供者
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider{

    //服务名称--实体映射 string：（interface+version+group) object:具体服务对象
    private final Map<String,Object> serviceMap;
    //注册过的服务名称
    private final Set<String> registerService;
    //服务注册对象
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registerService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 在服务提供器内添加服务的相关信息（没有添加到注册中心）
     * @param rpcServiceConfig 服务相关配置信息
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        //检测是否以及添加过一次
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registerService.contains(rpcServiceName)){
            return ;
        }
        registerService.add(rpcServiceName);
        //添加到服务实体映射中
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("添加服务: {} 和接口:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 获取服务实例
     * @param rpcServiceName 服务名称
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        //不存在该服务
        Object service = serviceMap.get(rpcServiceName);
        if(service==null){
           throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
       }
       return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try{
            //获取本地ip地址
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            //向注册中心进行服务注册
            serviceRegistry.registryService
                    (rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(hostAddress,9998));
        }catch(UnknownHostException e){
            log.error("获取本机地址时出现未知异常:",e.getMessage());
        }

    }
}
