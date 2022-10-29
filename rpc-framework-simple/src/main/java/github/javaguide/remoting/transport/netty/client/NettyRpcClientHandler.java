package github.javaguide.remoting.transport.netty.client;

import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author zhp
 * @date 2022-10-27 7:01
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    //未处理消息集合
    private final UnprocessedRequest unprocessedRequest;
    //Netty客户端
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        unprocessedRequest = SingletonFactory.getInstance(UnprocessedRequest.class);
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 处理服务端响应消息
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try{
            log.info("客户端接受服务端消息[{}]",msg);
            if(msg instanceof RpcMessage){
                RpcMessage message = (RpcMessage) msg;
                byte messageType = message.getMessageType();
                //心跳
                if(messageType== RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heartBeat [{}]",message.getData());
                }else if(messageType == RpcConstants.RESPONSE_TYPE){
                    //rpc响应消息
                    RpcResponse<Object> response  = (RpcResponse<Object>) message.getData();
                    //到未完成任务集合中进行登记
                    unprocessedRequest.complete(response);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用户事件触发
     * 检测到客户端的写状态空闲，则向服务端发送心跳，维持连接。
     * @param ctx
     * @param evt
     * @throws Exception
     */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState idleState = ((IdleStateEvent)evt).state();
            //写空闲 发送心跳到服务端维持连接
            if(idleState == IdleState.WRITER_IDLE){
                //log.info("写空闲发生 [{}]",ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                //获取连接
                Channel channel = nettyRpcClient.getConnect((InetSocketAddress) ctx.channel().remoteAddress());
                //监听发生结果
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
               // log.info("客户端发送心跳");
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端捕获异常",cause);
        cause.printStackTrace();
        ctx.close();
    }
}
