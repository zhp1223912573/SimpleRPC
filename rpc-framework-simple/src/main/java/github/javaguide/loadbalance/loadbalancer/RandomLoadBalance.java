package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.loadbalance.AbstracLoadBalance;
import github.javaguide.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @author zhp
 * @date 2022-10-25 18:48
 * 随机负载均衡
 */
public class RandomLoadBalance extends AbstracLoadBalance {

    /**
     * 随机选择返回的服务地址
     * @param serviceUrlList 服务地址集合
     * @param rpcRequest 服务请求
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
