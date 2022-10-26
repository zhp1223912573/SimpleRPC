package github.javaguide.config;

import lombok.*;

/**
 * @author zhp
 * @date 2022-10-26 12:38
 * rpc服务配置类
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    //服务版本号
    private String version;
    //同一接口服务可能存在多个实现，区别不同类的具体实现
    private String group;
    //执行服务的具体对象
    private Object service;

    public String getRpcServiceName(){
        return this.getServiceName()+this.version+this.group;
    }

    private String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
