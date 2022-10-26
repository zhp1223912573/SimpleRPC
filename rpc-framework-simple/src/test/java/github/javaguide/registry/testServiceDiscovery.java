package github.javaguide.registry;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.dto.RpcRequest;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

/**
 * @author zhp
 * @date 2022-10-26 12:48
 */
public class testServiceDiscovery {
    @Test
    public void test(){
        ServiceDiscovery zk = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        InetSocketAddress test = zk.lookupService(new RpcRequest().builder().interfaceName("github.javaguide.provider.service")
                .version("2.0").group("test").build());
    }
}
