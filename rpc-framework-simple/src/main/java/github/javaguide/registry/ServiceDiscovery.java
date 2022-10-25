package github.javaguide.registry;

import github.javaguide.extension.SPI;
import github.javaguide.remoting.dto.RpcReuqest;

import java.net.InetSocketAddress;

/**
 *
 * @author zhp
 * @date 2022-10-25 15:26
 *
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 根据服务名称查询服务地址
     * @param rpcReuqest 服务请求
     * @return
     */
    InetSocketAddress lookupService(RpcReuqest rpcReuqest);
}
