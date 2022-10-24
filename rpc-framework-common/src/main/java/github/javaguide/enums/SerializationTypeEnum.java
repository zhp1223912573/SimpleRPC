package github.javaguide.enums;

import com.sun.xml.internal.ws.developer.Serialization;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhp
 * @date 2022-10-24 14:59
 * 序列化类类型
 * 包含Kryo，hessian，protostubuff
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    KYRO((byte)0x01,"kyro"),
    PROTOSTUFF((byte)0x02,"protostuff"),
    HESSIAN((byte)0x03,"hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(SerializationTypeEnum s:SerializationTypeEnum.values()){
            if(s.code==code){
                return s.name;
            }
        }
        return null;
    }


}
