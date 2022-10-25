package github.javaguide.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import github.javaguide.exception.SerializeException;
import github.javaguide.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author zhp
 * @date 2022-10-25 14:48
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();){
            HessianOutput hessianOutput = new HessianOutput(baos);
            hessianOutput.writeObject(obj);
            return baos.toByteArray();
        }catch (IOException e){
            throw new SerializeException("hessian序列化异常");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object o = hessianInput.readObject();

            return clazz.cast(o);

        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
