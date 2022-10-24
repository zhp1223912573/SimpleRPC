package github.javaguide.extension;

import github.javaguide.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

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
            throw new IllegalArgumentException("扩展类型不应该为空！");
        }
        if(type.isInterface()){
            throw new IllegalArgumentException("扩展类型必须为interface类型！");
        }
        if(type.isAnnotationPresent(SPI.class)){
            throw new IllegalArgumentException("扩展类型必须被@SPI接口标记！");
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

    /**
     * 获取具体实现类实例对象
     * @param name 在配置文件中的名称
     * @return
     */
    public T getExtension(String name){
        //检验名称是否符合标准
        if(StringUtil.isBlank(name)){//检测是否有空格
            throw new IllegalArgumentException("扩展名不能为空或者包含空格！");
        }
        
        //尝试从缓存中读取，若不存在，则创建一个新的
        Holder<Object> holder = cachedInstance.get(name);
        if(holder==null){
            cachedInstance.putIfAbsent(name,new Holder<>());
            holder = cachedInstance.get(name);
        }

        Object instance =  holder.getValue();//获取具体类对象
        if(instance==null){ //双重检验，避免重复加载
            synchronized (holder){
                instance = (T) holder.getValue();
                if(instance==null){
                    instance = createExtension(name);
                    holder.setValue(instance);
                }
            }
        }
        return (T)instance;
    }

    /**
     * 创建具体实现类的实例对象
     * @param name 在配置文件中的名称
     * @return
     */
    private T createExtension(String name){
        //获取所有具体实现类的Class信息，从中找到name的类型信息
         Class<?> clazz = getExtensionClasses().get(name);
         if(clazz==null){
             throw new IllegalArgumentException("不存在名称为："+name+"的类型");
         }
        //尝试从缓存中找到name的类型的实例，不存在则新建并保存一个
        T instance =(T) EXTENSION_INSTANCE.get(clazz);
         if(instance==null){
             try{
                 EXTENSION_INSTANCE.putIfAbsent(clazz,clazz.newInstance());
                 instance = (T) EXTENSION_INSTANCE.get(clazz);
             }catch(Exception e){
                 log.error(e.getMessage());
             }
         }

         return instance;
    }

    /**
     * 获取所有具体实现类名称（简称）到其对应的类型信息映射
     * @return
     */
    private Map<String,Class<?>> getExtensionClasses() {
        //查询缓存，不存在的话新建
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if(classes==null){//双重检测
            synchronized (cachedClasses){
                classes = cachedClasses.getValue();
                if(classes==null){
                    classes = new HashMap<>();
                    //开始读取目录文件
                    loadDirectory(classes);
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 读取文件目录，加载所有配置文件的具体类型的相关信息
     * @param classes
     */
    private void loadDirectory(Map<String, Class<?>> classes) {
        //生成要加载的文件路径
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();//默认路径+当前扩展接口全名
        //读取文件目录路径下的所有配置文件
        try{
            Enumeration<URL> urls;
            ClassLoader classLoader =  ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if(urls!=null){
                while(urls.hasMoreElements()){
                    URL url = urls.nextElement();
                    //读取指定文件内容
                    loadResource(classes,classLoader,url);
                }
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }

    /**
     * 读取并解析特定配置文件内容，获取对应名称（简称）并加载其对应的类型，同时进行保存
     * @param classes
     * @param classLoader
     * @param url
     *
     * 文件格式如下：
     *  name=全类限定名
     *  zk=github.javaguide.registry.zk.ZkServiceRegistryImpl
     *  （可能存在‘#’,代表注释）
     *
     */
    private void loadResource(Map<String, Class<?>> classes, ClassLoader classLoader, URL url) {
        //读取特定文件内容
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),UTF_8))){
            String line ;
            while((line=reader.readLine())!=null){
                //先去除注释
                int commentIndex = line.lastIndexOf("#");
                if(commentIndex>0){
                    line = line.substring(0,commentIndex);
                }
                line = line.trim();//去除空格
                if(line.length()>0){
                    try{
                        int equalndex = line.lastIndexOf("=");
                        String name = line.substring(0,equalndex);
                        String clazzName = line.substring(equalndex+1).trim();
                        if(name.length()>0&&clazzName.length()>0){
                            //加载具体类
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            classes.put(name,clazz);
                        }
                    }catch(ClassNotFoundException e){
                        log.error(e.getMessage());
                    }
                }
            }
        }catch(IOException ex){
            log.error(ex.getMessage());
        }
    }
}
