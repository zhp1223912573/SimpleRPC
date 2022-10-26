package github.javaguide.remoting.transport.socket;

import github.javaguide.config.CustomShutdownHook;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.registry.ServiceRegistry;
import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author zhp
 * @date 2022-10-26 12:51
 * 基于socket的rpcrequst处理客户端
 */
@Slf4j
public class SocketRpcServer {
    //处理客户端链接
    private final ExecutorService threadpool;
    //服务注册
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        this.threadpool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        this.serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){

        try(ServerSocket server = new ServerSocket()){
            //监听本地端口
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(hostAddress,9998));
            //注册一个hook，在服务器退出后，释放相关资源
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket = null;
            //开始阻塞监听端口上是否有客户端连接
            while((socket=server.accept())!=null){
                //进入循环内部，说明出现一个客户端连接,创建线程进行rpc请求处理
                threadpool.execute(new SocketRpcRequestHandlerRunnable(socket));
                log.info("客户端 [{}] 建立链接",socket.getInetAddress() );
            }
            //释放所有的线程资源
            threadpool.shutdown();
        }catch(IOException e){
            log.error("尝试IOException",e);
        }
        //
    }
}
