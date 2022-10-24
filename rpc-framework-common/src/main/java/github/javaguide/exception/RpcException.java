package github.javaguide.exception;

import github.javaguide.enums.RpcErrorMessageEnum;

/**
 * @author zhp
 * @date 2022-10-24 15:31
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum,String detail){
        super(rpcErrorMessageEnum.getMessage()+":"+detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
