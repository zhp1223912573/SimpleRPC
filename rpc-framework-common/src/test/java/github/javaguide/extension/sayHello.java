package github.javaguide.extension;

/**
 * @author zhp
 * @date 2022-10-24 20:09
 */
@SPI
public interface sayHello {
    public void sayHello(String name);
}
