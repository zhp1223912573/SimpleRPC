package github.javaguide.util.threadpool;

import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import org.junit.jupiter.api.Test;
import sun.security.krb5.internal.crypto.Aes128;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zhp
 * @date 2022-10-24 21:04
 */
public class testThreadPoolFactoryUtil {
    static volatile int anInt = 0;
    static volatile int bnInt = 0;
    @Test
    public void test1(){
        ExecutorService es1 = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("测试1");
        ExecutorService es2 = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("测试2");
        for(int i=0;i<10;i++){
            es1.execute(()->{
                System.out.println("线程1：输出"+anInt);
                anInt++;
            });
            es2.execute(()->{
                System.out.println("线程2：输出"+bnInt);
                bnInt++;
            });
        }
     ThreadPoolFactoryUtil.shutdownAllThreadPool();
    }
}
