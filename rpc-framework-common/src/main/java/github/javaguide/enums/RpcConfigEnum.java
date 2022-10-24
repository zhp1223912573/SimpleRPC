package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhp
 * @date 2022-10-24 14:57
 * Rpc配置参数枚举类
 * 在调用CuratorFrameWork时会到定义的配置文件名称去查找于zk的地址配置信息，
 * 如果存在的话，从配置文件中读取。
 * 反之直接在CuratorFrameWork调用类内部设定的默认地址。
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
