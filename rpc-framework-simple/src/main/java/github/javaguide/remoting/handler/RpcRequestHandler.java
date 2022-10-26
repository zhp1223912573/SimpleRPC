package github.javaguide.remoting.handler;

import github.javaguide.exception.RpcException;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.ZkServiceProviderImpl;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zhp
 * @date 2022-10-26 12:56
 * Rpc请求处理器
 * 当有rpcrequest到达时，调用对应的服务，并执行，随后将执行结果返回。
 */
@Slf4j
public class RpcRequestHandler {
    //服务提供器，调用内部方法实现方法查找和运行
    private final ServiceProvider serviceProvider ;

    public RpcRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 调用rpcrequest请求的方法，执行并返回结果
     * @param rpcRequest
     * @return
     */
    public Object invoke(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getServiceName());
        return invokerTargetService(rpcRequest,service);
    }

    /**
     * 得到方法执行结果
     * @param rpcRequest
     * @param service
     * @return
     */
    private Object invokerTargetService(RpcRequest rpcRequest, Object service) {
        //方法执行结果
        Object result ;
        try{
            //调用方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务 [{}] 成功调用方法 [{}]",rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            throw new RpcException(e.getMessage(),e );
        }
        return result;
    }
}
