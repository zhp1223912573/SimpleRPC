package github.javaguide.loadbalance;

import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.utils.CollectionUtil;

import java.util.List;

/**
 * @author zhp
 * @date 2022-10-25 18:44
 */
public abstract class AbstracLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if(CollectionUtil.isEmpty(serviceUrlList)){
                return null;
        }

        //只剩一个了，直接返回，还选啥
        if(serviceUrlList.size()==1){
            return serviceUrlList.get(0);
        }

        return doSelect(serviceUrlList, rpcRequest);

    }

    /**
     * 负载均衡选择具体执行的服务地址
     *
     * @param serviceUrlList 服务地址集合
     * @param rpcRequest 服务请求
     * @return
     */
    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);
}
