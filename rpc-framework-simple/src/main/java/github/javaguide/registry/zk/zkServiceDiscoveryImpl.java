package github.javaguide.registry.zk;

import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.dto.RpcReuqest;

import java.net.InetSocketAddress;

/**
 * @author zhp
 * @date 2022-10-25 15:37
 */
public class zkServiceDiscoveryImpl implements ServiceDiscovery {
    @Override
    public InetSocketAddress lookupService(RpcReuqest rpcReuqest) {
        return null;
    }
}
