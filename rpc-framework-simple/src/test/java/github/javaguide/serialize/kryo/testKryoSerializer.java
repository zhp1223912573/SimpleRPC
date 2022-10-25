package github.javaguide.serialize.kryo;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.serialize.Serializer;
import org.checkerframework.checker.units.qual.K;
import org.junit.jupiter.api.Test;

/**
 * @author zhp
 * @date 2022-10-24 22:34
 */

public class testKryoSerializer {
    @Test
    public  void testKryo(){
        student s1 = new student("zhp","nan");
        KryoSerializer kryoSerializer =  new KryoSerializer();
        byte[] serialize = kryoSerializer.serialize(s1);
        student deserialize = kryoSerializer.deserialize(serialize, student.class);
        assert s1.getName().equals(deserialize.getName());
        assert s1.getGender().equals(deserialize.getGender());

    }

    @Test
    public void testKryoWithExtensionsLoader(){

        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer kryoSerializer = extensionLoader.getExtension("kryo");
        student s1 = new student("zhp","nan");
        byte[] serialize = kryoSerializer.serialize(s1);
        student deserialize = kryoSerializer.deserialize(serialize, student.class);
        assert s1.getName().equals(deserialize.getName());
        assert s1.getGender().equals(deserialize.getGender());
    }
}
