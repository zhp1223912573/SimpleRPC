package github.javaguide.provider;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.extension.SPI;

/**
 * @author zhp
 * @date 2022-10-26 12:34
 * 服务提供接口
 */
@SPI
public interface ServiceProvider {

    /**
     * 添加服务
     * @param rpcServiceConfig 服务相关配置信息
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务实例
     * @param rpcServiceName 服务名称
     * @return 服务实例对象
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     * @param rpcServiceConfig 服务相关配置信息
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
