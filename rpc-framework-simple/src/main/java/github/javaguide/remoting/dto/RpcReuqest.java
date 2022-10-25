package github.javaguide.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author zhp
 * @date 2022-10-25 15:16
 * Rpc请求
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class RpcReuqest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;//请求id
    private String interfaceName;//请求接口名
    private String methodName;//请求方法名
    private Object[] parameters;//参数
    private Class<?>[] paramTypes;//参数类型
    private String version;//版本号
    private String group;//一个接口可能存在多个实现


    public String getServiceName(){
        return this.getInterfaceName()+this.getGroup()+this.getVersion();
    }
}
