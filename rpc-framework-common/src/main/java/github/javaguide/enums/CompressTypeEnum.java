package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhp
 * @date 2022-10-24 14:56
 * 压缩类型枚举类
 * 对序列化后的数据再一次进行压缩
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte)0x01,"gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(CompressTypeEnum c: CompressTypeEnum.values()){
            if(c.code==code){
                return c.name;
            }
        }
        return null;
    }
}
