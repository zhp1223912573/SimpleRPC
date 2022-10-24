package github.javaguide.utils;

import java.util.Collection;
import java.util.Collections;

/**
 * @author zhp
 * @date 2022-10-24 19:58
 * 集合工具类
 */
public class CollectionUtil {
    /**
     * 判断集合类为不为空
     * @param c
     * @return
     */
    public static boolean isEmpty(Collection<?> c){
        return c==null||c.isEmpty();
    }
}
