package github.javaguide.serialize.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.javaguide.exception.SerializeException;
import github.javaguide.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author zhp
 * @date 2022-10-24 21:49
 * Kryo序列化效率高，但只适用于java
 */
public class KryoSerializer implements Serializer {

    //kyrp线程不安全，使用ThreadLocal包裹
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        //注册要序列化的类型
//        kryo.register(RpcRequest.Class);
//        kryo.register(RpcResponse.Class);
        kryo.register(student.class);

        return kryo;
    }) ;

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            //获取kryo
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            //手动释放，避免内存泄露
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("序列化失败！");
        }
    }


    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Input input = new Input(bais)){
            //获取kryo
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            Object object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(object);
        }catch(Exception ex){
            throw new SerializeException("反序列化失败!");
        }
    }
}
