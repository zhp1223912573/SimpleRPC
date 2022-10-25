package github.javaguide.registry.zk;

import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.loadbalance.LoadBalance;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.registry.zk.util.CuratorUtils;
import github.javaguide.remoting.dto.RpcReuqest;
import github.javaguide.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author zhp
 * @date 2022-10-25 15:37
 */
@Slf4j
public class zkServiceDiscoveryImpl implements ServiceDiscovery {
    private LoadBalance loadBalance;

    public zkServiceDiscoveryImpl() {
        loadBalance = ExtensionLoader
                .getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcReuqest rpcReuqest) {
        //获取请求的服务名，得到该服务的所有服务地址集合
        String serviceName = rpcReuqest.getServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChilderenNodes(zkClient, serviceName);

        //先判断是否为空
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            //找不到服务地址，抛出异常
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, serviceName);
        }

        //从服务地址结合中找到最合适的服务（通过负载均衡算法决定）
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcReuqest);
        log.info("成功找到服务地址[{}]", targetServiceUrl);

        //分割得到ip地址和端口号
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);

        return new InetSocketAddress(host,port);
    }
}
