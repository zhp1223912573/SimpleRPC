package github.javaguide.serialize.hessian;

import github.javaguide.serialize.kryo.KryoSerializer;
import github.javaguide.serialize.kryo.student;
import org.junit.jupiter.api.Test;

/**
 * @author zhp
 * @date 2022-10-25 14:44
 */
public class testHessianSerializer {
    @Test
    public void test(){
        student s1 = new student("zhp","nan");
        HessianSerializer hessianSerializer =  new HessianSerializer();
        byte[] serialize = hessianSerializer.serialize(s1);
        student deserialize = hessianSerializer.deserialize(serialize, student.class);
        assert s1.getName().equals(deserialize.getName());
        assert s1.getGender().equals(deserialize.getGender());
    }
}
