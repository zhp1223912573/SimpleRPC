package github.javaguide.remoting.transport.socket;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author zhp
 * @date 2022-10-26 13:32
 * 服务端接受到客户端的服务请求（rpcRequest)后，创建一个当前线程执行
 * 请求方法，并返回运行结果
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private final Socket socket ;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     *获取连接的输入输出流，得到rpcrequet，通过该请求调用handler对应的处理方法，返回执行结果
     */
    @Override
    public void run() {
        log.info("服务端处理客户端发送请求： [{}]", Thread.currentThread().getName());
        try(ObjectInputStream objectInputStream= new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())){
            //获取服务请求
            RpcRequest rpcRequest =(RpcRequest) objectInputStream.readObject();
            //调用服务请求方法
            Object result = rpcRequestHandler.invoke(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
            //传输
            objectOutputStream.flush();
        }catch(IOException | ClassNotFoundException e){
            log.error("服务端出来客户端请求失败！",e.getMessage());
        }
    }
}
