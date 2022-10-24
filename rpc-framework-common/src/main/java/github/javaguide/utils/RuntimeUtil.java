package github.javaguide.utils;

/**
 * @author zhp
 * @date 2022-10-24 19:48
 * 运行时工具类
 */
public class RuntimeUtil {

    /**
     * 获取CPU核心数量
     * @return cpu核心数量
     */
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }
}
