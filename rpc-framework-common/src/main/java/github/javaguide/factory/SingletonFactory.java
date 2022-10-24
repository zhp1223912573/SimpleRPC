package github.javaguide.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhp
 * @date 2022-10-24 18:03
 * 获取单例对象的工厂
 */
public class SingletonFactory {
    //String为Object的全类限定名，Object为具体实例对象
    private static final Map<String,Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    /**
     * 生成单列对象
     * @param c
     * @param <T>
     * @return
     */
    public static <T> T getInstance(Class<T> c){
        if(c==null){
            throw new IllegalArgumentException("类型信息不能为空！");
        }

        String key = c.toString();
        //查看是否已经创建
        if(OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        }else{
            //不存在则新建并返回一个
            return c.cast(OBJECT_MAP.computeIfAbsent(key,k -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }
}
