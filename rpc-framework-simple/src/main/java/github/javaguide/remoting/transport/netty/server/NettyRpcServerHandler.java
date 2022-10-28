package github.javaguide.remoting.transport.netty.server;

import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhp
 * @date 2022-10-28 15:09
 * 服务端请求处理器
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try{
            //判断是否为RpcMessage
            if(msg instanceof RpcMessage){
                //获取消息类型，设置RpcResponse
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                //判断是否为心跳信息
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    String data =(String) ((RpcMessage) msg).getData();
                    log.info("hearBeat [{}]",data);
                    //返回心跳响应
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }
                //判断是否为RpcRequest
                else if(messageType ==RpcConstants.REQUEST_TYPE){
                    //获取rpcReuqest,调用服务并封装返回结果
                    RpcRequest rpcRequest  = (RpcRequest) (((RpcMessage)msg).getData());
                    //调用服务
                    Object result = rpcRequestHandler.invoke(rpcRequest);
                    log.info(String.format("服务端获取方法执行结果: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    //检查channel状态
                    if(ctx.channel().isActive()&&ctx.channel().isWritable()){
                        RpcResponse<Object> success = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(success);
                    }else{
                        RpcResponse<Object> fail = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(fail);
                        log.error("channel无法写入，信息丢失");
                    }
                }
                //将响应写入通道
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }finally {
            //释放内存
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            //30秒内没有监听到客户端连接请求，直接关闭连接
            if(state == IdleState.READER_IDLE){
                log.info("空闲检查发生，关闭连接");
                ctx.close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.error("服务端捕获异常");
        cause.printStackTrace();
        ctx.close();
    }
}
