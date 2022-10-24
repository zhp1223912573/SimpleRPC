package github.javaguide;

import github.javaguide.factory.SingletonFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zhp
 * @date 2022-10-24 19:41
 */
public class SingletonFactoryTest {
    @Test
    public void testGetSingleton(){
        ArrayList instance = SingletonFactory.getInstance(ArrayList.class);
        instance.add(1);
        ArrayList instance1 =  SingletonFactory.getInstance(ArrayList.class);
        assertEquals(instance1,instance);
    }
}
