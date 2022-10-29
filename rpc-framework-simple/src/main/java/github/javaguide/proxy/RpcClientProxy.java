package github.javaguide.proxy;

import github.javaguide.config.RpcServiceConfig;
import github.javaguide.enums.RpcErrorMessageEnum;
import github.javaguide.enums.RpcResponseCodeEnum;
import github.javaguide.exception.RpcException;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.transport.RpcRequestTransport;
import github.javaguide.remoting.transport.netty.client.NettyRpcClient;
import github.javaguide.remoting.transport.socket.SocketRpcClient;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author zhp
 * @date 2022-10-26 14:59
 * 在tesSocketRpcServer,testSocketRpcClient中我们测试了Socker网络传输机制下，
 * 一次完成的rpc请求的发送以及响应过程。该过程实现了rpc的绝大部分功能，
 * 在本地端调发现注册中中要调用远程方法的地址，通过该地址调用远程端的服务方法，最后返回调用结果。
 *
 * 但是在上述两个测试函数中，我们需要在服务端手动发布服务到注册中心上，在客户端上有需要手动设置rpcrequest调用请求
 * 不符合RPC框架“调用远端方法就像调用本地方法一样方便”的特性。
 * 所以，我们需要一个RpcClientProxy，即Rpc请求客户端代理类。
 * 通过该代理类实现对网络传输过程，以及服务发起请求所需数据填充过程的封装，让一个代理类来实现这一繁琐的过程，
 * 我们仅仅需要调用需要使用服务的接口下的方法，就能得到运行结果。
 *
 * 基于上述需求，我们需要采取基于jdk的动态代理
 *
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    //向服务端发送rpcrequest，有socket和netty两种实现
    private final RpcRequestTransport rpcRequestTransport;
    //请求服务配置信息
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * 获取代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    /**
     * 客户端使用代理类调用接口方法，对该方法进行拦截，通过该方法实现网络传输的封装，以及具体方法的调用
     * @param proxy 代理对象
     * @param method  调用方法
     * @param args  方法参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("方法 [{}] 被调用",method.getName());
        //封装rpcrequest请求
        RpcRequest rpcRequest = new RpcRequest().builder()
                .interfaceName(method.getDeclaringClass().getName())
                .parameters(args)
                .requestId(UUID.randomUUID().toString())
                .methodName(method.getName())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .paramTypes(method.getParameterTypes())
                .build();
        RpcResponse<Object> rpcResponse = null;


        //基于socket传输
        if(rpcRequestTransport instanceof SocketRpcClient){
            rpcResponse = (RpcResponse<Object>)rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        //基于Netty传输
        if(rpcRequestTransport instanceof NettyRpcClient){
           CompletableFuture<RpcResponse<Object>> completeableFuture =
                    (CompletableFuture<RpcResponse<Object>>)rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completeableFuture.get();
        }
        //对服务请求和响应进行检验
        check(rpcResponse,rpcRequest );

        //返回执行结果
        return rpcResponse.getData();
    }

    /**
     * 对rpc请求和响应进行检测
     * @param rpcResponse
     * @param rpcRequest
     */
    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        //响应为空，服务调用失败
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        //响应id和请求id不一致，请求响应不匹配
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        //响应码为空或为失败，服务调用失败
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
