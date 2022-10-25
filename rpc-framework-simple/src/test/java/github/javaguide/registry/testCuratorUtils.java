package github.javaguide.registry;

import github.javaguide.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author zhp
 * @date 2022-10-25 18:36
 */
public class testCuratorUtils {
    @Test
    public void testGetZKClient(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childerenNodes = CuratorUtils.getChilderenNodes(zkClient, "github.javaguide.HelloService");
        System.out.println("获取子节点信息");
        for(String s:childerenNodes){
            System.out.println(s);
        }

        List<String> childerenNodes1 = CuratorUtils.getChilderenNodes(zkClient, "github.javaguide.HelloServicetest1version1");
        for(String s:childerenNodes1){
            System.out.println(s);
        }

        CuratorUtils.createPersisentNode(zkClient,"/my-rpc/github.javaguide.我新建的测试节点");
        CuratorUtils.clearRegistry(zkClient,new InetSocketAddress("172.19.208.1",9998));
        return;
    }
}
