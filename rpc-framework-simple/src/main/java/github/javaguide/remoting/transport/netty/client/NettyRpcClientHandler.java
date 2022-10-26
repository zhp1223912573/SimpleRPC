package github.javaguide.remoting.transport.netty.client;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.dto.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhp
 * @date 2022-10-27 7:01
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequest unprocessedRequest;
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
                if(messageType)
            }
        }

    }
}
