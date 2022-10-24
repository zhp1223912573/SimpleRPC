package github.javaguide.extension;

/**
 * @author zhp
 * @date 2022-10-24 20:10
 */
public class sayHelloLoud implements sayHello{
    @Override
    public void sayHello(String name) {
        System.out.println("你好，"+name+"(loudly)");
    }
}
