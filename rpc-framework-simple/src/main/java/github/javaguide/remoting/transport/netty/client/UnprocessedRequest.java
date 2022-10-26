package github.javaguide.remoting.transport.netty.client;

import com.sun.xml.internal.ws.util.CompletedFuture;
import github.javaguide.remoting.dto.RpcResponse;

import javax.xml.ws.Response;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhp
 * @date 2022-10-27 7:09
 * 存储服务端未处理的请求
 */
public class UnprocessedRequest {
    //保存服务端未处理完成的请求，string--requsetId
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    /**
     * 客户端发送rpc请求时，使用completedFuture保存返回结果，客户端提前返回，异常等待
     * @param requestId
     * @param future
     */
    public void put(String requestId,CompletableFuture<RpcResponse<Object>> future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,future);
    }

    public UnprocessedRequest() {
    }

    /**
     * 客户端监听到来自服务端的消息，从未处理队列中取出该requestId对应消息保存对象futrue，
     * 将返回消息装入future
     */
    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if(future!=null){
            future.complete(rpcResponse);
        }else{
            throw new IllegalStateException();
        }
    }

}
