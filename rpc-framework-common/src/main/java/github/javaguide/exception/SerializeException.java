package github.javaguide.exception;

import github.javaguide.enums.SerializationTypeEnum;

/**
 * @author zhp
 * @date 2022-10-24 15:30
 * 序列化异常
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String message){
        super(message);
    }
}
