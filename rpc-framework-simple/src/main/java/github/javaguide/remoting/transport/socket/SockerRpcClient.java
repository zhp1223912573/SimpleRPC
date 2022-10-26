package github.javaguide.remoting.transport.socket;

import github.javaguide.exception.RpcException;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.transport.RpcRequestTransport;
import jdk.net.Sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author zhp
 * @date 2022-10-26 13:22
 * 基于socket的rpcrequest传输
 */
public class SockerRpcClient implements RpcRequestTransport {

    //服务发现
    private final ServiceDiscovery serviceDiscovery;

    public SockerRpcClient() {
        this.serviceDiscovery = SingletonFactory.getInstance(ServiceDiscovery.class);
    }

    /**
     *
     * @param rpcRequest
     * @return rpc请求结果
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //查找要执行的服务的远端地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        //创建socket连接，向服务端发送rpcquest请求，同步获取结果
        try(Socket socket = new Socket()){
            //创建连接
            socket.connect(inetSocketAddress);
            //获取输出流
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //写入请求
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        }catch(IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }
    }
}
