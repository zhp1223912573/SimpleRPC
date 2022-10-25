package github.javaguide.registry;

import github.javaguide.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author zhp
 * @date 2022-10-25 15:27
 */
@SPI
public interface ServiceRegistry {
    /**
     * 根据服务地址和名称进行服务注册
     * @param serviceName   服务名称（class name + group + version)
     * @param inetSocketAddress 远程服务地址
     */
    void registryService(String serviceName,InetSocketAddress inetSocketAddress);
}
