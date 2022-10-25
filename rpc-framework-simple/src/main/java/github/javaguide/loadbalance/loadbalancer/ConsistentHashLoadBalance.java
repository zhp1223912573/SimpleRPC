package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.loadbalance.AbstracLoadBalance;
import github.javaguide.remoting.dto.RpcReuqest;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhp
 * @date 2022-10-25 18:51
 *
 * 一致性哈希算法基本介绍：
 * https://blog.csdn.net/JavaMoo/article/details/79646095?ops_request_misc=&request_id=&biz_id=102&utm_term=%E4%B8%80%E8%87%B4%E6%80%A7%E5%93%88%E5%B8%8C%E8%B4%9F%E8%BD%BD%E5%9D%87%E8%A1%A1&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-6-79646095.142^v59^js_top,201^v3^control_2&spm=1018.2226.3001.4187
 *
 * 参考dubbo的一致性哈希实现：
 * https://www.cnblogs.com/konghuanxi/p/16591298.html
 *
 * MessageDigest相关简介：
 *https://www.jianshu.com/p/b419163272c1
 */
public class ConsistentHashLoadBalance extends AbstracLoadBalance {
    //string为服务名称 consistentHashSelector为其对应的选择器（用于一致性hash找查找）
    private final ConcurrentHashMap<String,ConsistentHashSelector>selectors = new ConcurrentHashMap<>();

    /**
     *
     * @param serviceUrlList 服务地址集合
     * @param rpcReuqest 服务请求
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcReuqest rpcReuqest) {
        //生成服务地址集合的身份id 用来标识唯一一个选择且
        int identityHashCode = System.identityHashCode(serviceUrlList);
        //获取服务名称
        String serviceName = rpcReuqest.getServiceName();
        ConsistentHashSelector selector = selectors.get(serviceName);
        //如果不存在，或者身份id不一致，再次创建一个，避免同一服务在多次调用期间生成新的服务地址节点
        if(selector==null|| identityHashCode!=selector.identityHashCode){
            //别忘了保存
            selectors.put(serviceName, new ConsistentHashSelector(serviceUrlList,160,identityHashCode));
            selector = selectors.get(serviceName);
        }
        //开始选择
        return selector.select(serviceName+ Arrays.stream(rpcReuqest.getParameters()));

    }


    /**
     * 一致性哈希选择器，真正执行负载均衡的类
     * 从某一服务的多个调用地址集合中抽取一个来调用，该选择器内部使用一个identityHashcode表示该地址集合。
     *
     */
    static class ConsistentHashSelector{

        //一致性哈希算法的逻辑“⚪”的具体实现 Long表示虚拟节点（某一服务的hash值），String表示该服务的具体地址
        private final TreeMap<Long,String> virtualInvokers;
        //身份id--hashcode 表示该选择器选择的地址集合
        private final int identityHashCode;


        /**
         *
         * @param invokers 调用服务地址集合
         * @param replicaNumber 虚拟节点个数，这里默认为160（我也不知道为什么要那么多，仿造dubbo的）
         * @param identityHashCode 表示invokers的hashcode
         */
        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

           //遍历每个地址，尝试将地址放置到⚪上
            for(String invoker : invokers){
                for(int i=0;i<replicaNumber/4;i++){
                    //通过md5算法单向哈希函数生成字节数组
                    byte[] digest = md5(invoker+i);
                    //对同一数组进行hash，根据hash次数不同，取digest的不同位，尽快能使虚拟节点分布均匀
                    for(int h=0;h<4;h++){
                        long m = hash(digest,h);
                        //将虚拟节点映射到⚪上--virtualInvokers
                        //虽然放入接⚪中的虚拟节点的hash不一致，但都表示一个服务，同时又尽可能的分布在⚪中
                        //解决了资源分配不均匀的问题
                        virtualInvokers.put(m,invoker);
                    }
                }
            }
        }


        /**
         * 下述两方法具体思路可以参考图片：dubbo-一致性hash实现源码.png
         * @param key
         * @return
         */

        static byte[] md5(String key) {
            MessageDigest md;
            try{
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);

            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        static long hash(byte[] digest,int idx){
            return ((long) (digest[3 + idx * 4] & 255) << 24
                    | (long) (digest[2 + idx * 4] & 255) << 16
                    | (long) (digest[1 + idx * 4] & 255) << 8
                    | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }


        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        /**
         * 选择距离当前hash最近的虚拟节点，返回该结点对应的服务地址
         * @param hash
         * @return
         */
        private String selectForKey(long hash) {
            //距离当前hash最近的结点entry
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hash, true).firstEntry();

            //如果hash已经为最大，则没有后续结点，我们需要手动返回第一个结点
            if(entry==null){
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }

    }
}
