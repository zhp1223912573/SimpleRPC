package github.javaguide.remoting.dto;

import github.javaguide.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author zhp
 * @date 2022-10-25 15:25
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID =715745410605631233L;
    private String requestId;//响应id
    private int code ;//响应状态码
    private String message;//响应信息
    private T data;//响应数据

    /**
     * 处理rpc请求成功，返回响应
     * @param data
     * @param requestId
     * @param <T>
     * @return
     */
    public <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if(data!=null){
            response.setData(data);
        }
        return response;
    }

    /**
     * 处理rpc请求失败，返回响应
     * @param rpcResponseCodeEnum
     * @param <T>
     * @return
     */
    public <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
