package github.javaguide.remoting.dto;

import lombok.*;

/**
 * @author zhp
 * @date 2022-10-27 7:24
 * 客户端与服务端直接除了直接的发送RpcRequest来明确调用的服务具体方法，以及参数类型与参数外，
 * 还可能在连接中发送诸如数据序列化类型，压缩类型，数据长度，可能有时候还需要发送心跳维持连接。
 * 所以在基于Netty的Rpc调用过程，使用RpcMessage包含上述的参数，并在data遍历中存储RpcRequest，
 * 实现多一层的逻辑封装。
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * rpc请求类型：
     * 正常rpc调用请求/响应类型
     * 心跳请求/响应类型
     */
    private byte messageType;

    /**
     * 序列化类型
     * hessian
     * kryo
     * protobuff
     */
    private byte codec;

    /**
     * 数据压缩类型
     * gzip
     */
    private byte compress;

    /**
     * 请求id
     */
    private int requestId;


    /**
     * 传输的具体数据
     * rpcrequest调用的相关信息
     */
    private Object data;
}
