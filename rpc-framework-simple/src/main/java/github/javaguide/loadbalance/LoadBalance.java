package github.javaguide.loadbalance;

import github.javaguide.extension.SPI;
import github.javaguide.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @author zhp
 * @date 2022-10-25 19:20
 * 负载均衡
 */
@SPI
public interface LoadBalance {
    /**
     * 从一系列服务地址中选择一个服务地址返回
     * @param serviceUrlList 服务地址集合
     * @param rpcRequest
     * @return 选择的服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
