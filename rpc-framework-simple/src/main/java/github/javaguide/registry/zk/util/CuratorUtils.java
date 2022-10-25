package github.javaguide.registry.zk.util;

import github.javaguide.enums.RpcConfigEnum;
import github.javaguide.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * CuratorFramework--zookeeper的客户端类
 * 封装curator功能生成该工具类
 * @author zhp
 * @date 2022-10-25 15:29
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;//尝试获取客户端连接，每次访问的中间休息时间
    private static final int MAX_RETRYS = 3;//最大尝试次数
    private static final String ZK_REFISTRY_ROOT_PATH = "/my-rpc";//服务节点的根路径
    //服务--地址映射缓存，一个服务可能存在多个远程地址
    private static final Map<String, List<String>> SERVICE_MAP_ADDRESS_CACHED = new ConcurrentHashMap<>();
    //已经注册的服务节点路径，避免重复添加
    private static final Set<String> REGISTRED_PATH_SET =  ConcurrentHashMap.newKeySet();
    //客户端连接
    private static CuratorFramework zkClient;
    //zookeeper默认地址
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    public CuratorUtils() {
    }

    /**
     * 获取zookeeper客户端连接
     * @return
     */
    public static CuratorFramework getZkClient(){
        //检测zkclient是否存在，且正常运行，如果是直接返回，不是再创建
        if(zkClient!=null&&zkClient.getState()== CuratorFrameworkState.STARTED){
            return zkClient;
        }

        //读取配置文件，获取zookeeper服务地址
        Properties properties = PropertiesUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress =
                properties!=null&&properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue())!=null
                ?properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()):DEFAULT_ZOOKEEPER_ADDRESS;

        //创建一个新的zkclient并返回
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRYS);   //重试策略，每次间隔时间会逐渐加长
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();

        zkClient.start();//开始连接

        try {
            //连接成功前阻塞30秒
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("连接zookeeper超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;

    }

    /**
     * 创建持久性节点
     * @param zkClient 客户端连接
     * @param path 节点名称
     */
    public static void createPersisentNode(CuratorFramework zkClient,String path){
        try{
            //检测是否已经存在
            if(REGISTRED_PATH_SET.contains(path)|| zkClient.checkExists().forPath(path)!=null){
                log.info("节点已经存在! 节点为：[{}]", path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点成功创建！节点为[{}]", path);
            }
            //不存在，新建一个节点
        }catch (Exception e){
            log.error("创建持久性节点 [{}] 失败", path);
        }

    }

    /**
     * 得到节点下的所有子节点
     * @param zkClient
     * @param rpcServiceName    service name eg:github.javaguide.HelloServicetest2version1
     * @return
     */
    public static List<String> getChilderenNodes(CuratorFramework zkClient,String rpcServiceName){
        //获取缓存
        if(SERVICE_MAP_ADDRESS_CACHED.containsKey(rpcServiceName)){
            return SERVICE_MAP_ADDRESS_CACHED.get(rpcServiceName);
        }
        //缓存中不存在，直接读取
        List<String> result = null;
        String servicePath  = CuratorUtils.ZK_REFISTRY_ROOT_PATH+"/"+rpcServiceName;//获取的服务名称
        try{
            result = zkClient.getChildren().forPath(servicePath);
            //缓存起来
            SERVICE_MAP_ADDRESS_CACHED.put(rpcServiceName,result);
            //设置监听
            registerWatcher(rpcServiceName, zkClient);
        }catch(Exception e){
            log.error("获取节点 [{}]下子节点失败", servicePath);
        }
        return result;
    }

    /**
     * 子节点监听
     * @param rpcServiceName
     * @param zkClient
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath =ZK_REFISTRY_ROOT_PATH +"/"+rpcServiceName;
        //设置子节点的监听
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        //子节点发生变化，重新读入缓存
        PathChildrenCacheListener pathChildrenCacheListener =  (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_MAP_ADDRESS_CACHED.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * 清除注册过的服务
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTRED_PATH_SET.stream().parallel().forEach(p->{
            try{
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch(Exception e){
                log.error("清除节点路径 [{}] 失败", p);
            }
        });
        log.info("zookeeper所有服务节点清除:[{}]", REGISTRED_PATH_SET.toString());

    }


}
