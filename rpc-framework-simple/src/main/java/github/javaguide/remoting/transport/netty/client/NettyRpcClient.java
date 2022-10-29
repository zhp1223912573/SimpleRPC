package github.javaguide.remoting.transport.netty.client;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.RpcRequestTransport;
import github.javaguide.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javaguide.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import javax.xml.ws.Response;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author zhp
 * @date 2022-10-27 7:24
 * 基于netty的Rpc客户端应用
 * 实现Netty客户端的基本配置。
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    //客户端服务发现器
    private final ServiceDiscovery serviceDiscovery;
    //客户端未处理完成rpc请求集合
    private final UnprocessedRequest unprocessedRequest;
    //客户端连接提供器
    private final ChannelProvider channelProvider;
    //客户端启动配置类
    private final Bootstrap bootstrap;
    //客户端响应器组
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(){
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        //初始化客户端链接资源
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //设置连接超时时间限制
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //如果超过5秒没有写入数据，发送心跳
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //保存返回结果
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //查询服务地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        //获取连接
        Channel channel = this.getConnect(inetSocketAddress);
        if(channel!=null&&channel.isActive()){
            //将异步请求结果保存到服务器未处理请求集合中，当handler获取到来自服务端的请求响应后，
            //再次当前集合取出该future对象，保存response返回对象
            unprocessedRequest.put(rpcRequest.getRequestId(),resultFuture);
            //封装RpcMessage
            RpcMessage rpcMessage = new RpcMessage().builder()
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .data(rpcRequest)
                    .build();
            //写入信息，调用监听器，监听消息是否正常到达
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future->{
                if(future.isSuccess()){
                    log.info("客户端rpcMessage [{}] 成功发送到达服务端", rpcMessage);
                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("消息发送失败！", future.cause());
                }
            });
        }else{
            throw new IllegalStateException();
        }

        //返回保存结果
        return resultFuture;
    }

    /**
     * 建立客户端到服务端的连接
     * @param inetSocketAddress
     * @return
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        //获取执行结果
        CompletableFuture<Channel> future = new CompletableFuture<>();
        //创建连接，并注册监听器，当服务端返回响应，处理响应。
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) lisenter->{
            if(lisenter.isSuccess()){
                log.info("客户端成功与服务端 [{}] 建立连接",inetSocketAddress.toString());
                //将返回的通道加入执行结果
                future.complete(lisenter.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        //等待连接请求响应，如果没有得到结果，一致阻塞直到正常返回。
        return future.get();
    }

    /**
     * 获取客户端服务端连接，若不存在，则新建一个连接
     * @param inetSocketAddress
     * @return
     */
    public Channel getConnect(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        //不存在，新建一个
        if(channel==null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(channel,inetSocketAddress);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
