package github.javaguide.config;

import github.javaguide.registry.zk.util.CuratorUtils;
import github.javaguide.remoting.transport.netty.server.NettyRpcServer;
import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 *
 * @author zhp
 * @date 2022-10-26 14:42
 * 当服务端下线后，将注册过的节点全部清除
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public CustomShutdownHook() {

    }


    public void clearAll(){
        log.info("addShutdownHook for clearAll");
        //在程序正常结束或出现异常导致程序终止后，创建一个线程调用该hook完成相关资源的清除和释放
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(hostAddress, NettyRpcServer.PORT);
                //清除注册的所有服务节点
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            }catch (UnknownHostException ex){
                log.error("资源释放出现错误！");
            }
            //释放线程池资源
            ThreadPoolFactoryUtil.shutdownAllThreadPool();
        }));
    }
}
