package github.javaguide.registry.zk;

import com.sun.jndi.cosnaming.CorbanameUrl;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author zhp
 * @date 2022-10-25 15:37
 */
public class zkServiceRegistryImpl implements ServiceRegistry {

    @Override
    public void registryService(String serviceName, InetSocketAddress inetSocketAddress) {
        //创建服务路径
        String servicePath = CuratorUtils.ZK_REFISTRY_ROOT_PATH+"/"+serviceName+inetSocketAddress.toString();
        //获取zookeeper客户端连接
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //通过连接将服务进行注册
        CuratorUtils.createPersisentNode(zkClient,servicePath);
    }
}
