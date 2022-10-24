package github.javaguide.extension;

import org.junit.jupiter.api.Test;

/**
 * @author zhp
 * @date 2022-10-24 20:12
 */
public class ExtensionLoaderTest {
    /**
     * 测似通过
     */
    @Test
    public void testExtensioLoader(){
        ExtensionLoader<sayHello> extensionLoader = ExtensionLoader.getExtensionLoader(sayHello.class);
        sayHello loud = extensionLoader.getExtension("loud");
        loud.sayHello("zhp");
        sayHello quiet = extensionLoader.getExtension("quiet");
        quiet.sayHello("zhp");
    }
}
