package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhp
 * @date 2022-10-24 14:59
 * Rpc响应码枚举类
 * 成功，失败两种状态
 */
@AllArgsConstructor
@Getter
public enum RpcResponseCodeEnum {

    SUCCESS(200,"The remote is success"),
    FAIL(500,"The remote is fail");

    private final int code;
    private final String message;
}
