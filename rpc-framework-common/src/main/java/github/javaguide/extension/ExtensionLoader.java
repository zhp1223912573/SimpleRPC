package github.javaguide.extension;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhp
 * @date 2022-10-24 15:44
 *
 * 一个自定义的SerivceLoader，实现对spi接口具体实现类的加载,并缓存实现类的相关信息达到优化效果。
 *
 *  SeriviceLoder相关介绍：https://javaguide.cn/java/basis/spi.html#%E8%87%AA%E5%B7%B1%E5%AE%9E%E7%8E%B0%E4%B8%80%E4%B8%AA-serviceloader
 * 实现参考：（refer to dubbo spi） https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 *
 * 具体作用：
 *  具体实现类的相关信息（全类名）都放在SERVICE_DIRECTORY下，当我们要获取具体实现类的实例对象，
 *  调用类中的getExtension(String name)，输入所需要的实现类在配置文件中的key值。
 *  随后，该类会加载对应类型信息，读取指定配置文件，通过key值得到具体需要类的全类名，随后通过反射生成实例对象，
 *  一系列过程中，会在ExtensionLoader内保存具体的相关信息（如class），也就是下述多个cachedXXX名称的Map的作用。
 *
 */
@Slf4j
public final class ExtensionLoader<T> {
    //具体实现类相关配置信息路径（系统自带ServiceLoder加载路径："META-INF/services"
    private static final String SERVICE_DIRECTORY = "META-INF/extensions";
    //缓存某一spi接口的扩展加载器，避免重复加载
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    //缓存某一spi接口的具体实现类实例对象，同样也是避免重复
    private static final Map<Class<?>,Object> EXTENSION_INSTANCE = new ConcurrentHashMap<>();

    //当前扩展加载器的类型，为被@SPI注解标记的接口类型
    private Class<?> type;
    //String为配置文件中具体实现类的名称（自定义的，如zookeeper的名称为zk）
    //Holder保存该名称代表的具体实现类的一个实例（通过缓存，可以直接返回，不需要再次读取配置文件中的信息，然后去加载）
    private Map<String,Holder<Object>> cachedInstance = new ConcurrentHashMap<>();
    //保存一个map，该map的String含义也和上述一样，是某个具体实现类在配置文件中的名称
    // 而class是该名称对应的具体实现类的class，我们使用他通过反射来实例化一个具体类的对象
    private Holder<Map<String,Class<?>>>cachedClasses = new Holder<>();

    public ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取特定类型的ExtensionLoder
     * @param type
     * @param <S>
     * @return
     */
    public static <S>ExtensionLoader<S> getExtensionLoader(Class<S> type){
        //先检验该类型是否为符合要求
        if(type==null){
            throw new IllegalStateException("扩展类型不应该为空！");
        }
        if(type.isInterface()){
            throw new IllegalStateException("扩展类型必须为interface类型！");
        }
        if(type.isAnnotationPresent(SPI.class)){
            throw new IllegalStateException("扩展类型必须被@SPI接口标记！");
        }
        //开始获取ExtensionLoader
        //先从缓存中读取，如果不存在再创建一个新的，并保持在缓存
        ExtensionLoader<S> extensionLoader =(ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if(extensionLoader==null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<>(type));
            extensionLoader  =(ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }


}
