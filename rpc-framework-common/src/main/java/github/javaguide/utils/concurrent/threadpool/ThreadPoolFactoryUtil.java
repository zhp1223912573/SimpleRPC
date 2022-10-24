package github.javaguide.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhp
 * @date 2022-10-24 20:35
 * 线程池工程工具类
 */
@Slf4j
public final class ThreadPoolFactoryUtil {

    /**
     * 通过threadNameprefix来区分不同线程池
     *我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix
     *  value: threadPool
     */
    private static final Map<String, ExecutorService>THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {
    }

    public static ExecutorService createCustomThreadPoolIfAbsent
            (String threadNamePrefix){
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return  createCustomThreadPoolIfAbsent(threadNamePrefix,customThreadPoolConfig,false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent
            (String threadNamePrefix,CustomThreadPoolConfig config ){
        return  createCustomThreadPoolIfAbsent(threadNamePrefix,config,false);
    }

    private static ExecutorService createCustomThreadPoolIfAbsent
            (String threadNamePrefix, CustomThreadPoolConfig config, Boolean daemon) {
        ExecutorService threadpool =THREAD_POOLS.computeIfAbsent(threadNamePrefix,
                key -> createThreadPool(threadNamePrefix,config,daemon));
        //如果threapool被shutdown了就重新创建一个
        if(threadpool.isShutdown()||threadpool.isTerminated()){
            THREAD_POOLS.remove(threadNamePrefix);
            threadpool = createThreadPool(threadNamePrefix,config,daemon);
            THREAD_POOLS.put(threadNamePrefix,threadpool);
        }
        return threadpool;
    }

    /**
     * 创建线程池
     * @param threadNamePrefix
     * @param config
     * @param daemon
     * @return
     */
    private static ExecutorService createThreadPool
            (String threadNamePrefix, CustomThreadPoolConfig config, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor
                (config.getCorePoolSize(),config.getMaximumPoolSize(),
                        config.getKeepAliveTime(),config.getUnit(),
                        config.getWorkQueue(),threadFactory);
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory
            (String threadNamePrefix, Boolean daemon) {
        if(threadNamePrefix!=null){
            if(daemon!=null){
                //创建线程工厂
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix+"-%d")
                        .setDaemon(daemon).build();
            }
        }else{
            return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix+"-%d").build();
        }

        return Executors.defaultThreadFactory();
    }

    /**
     * 关闭所有线程池
     */
    public static void shutdownAllThreadPool(){
        log.info("调用shutdownAllThreadPool方法");
        //利用并发流关闭线程池
        THREAD_POOLS.entrySet().parallelStream().forEach(entry->{
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池 [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try{
                //等待10秒后还没有关闭，抛出异常
                executorService.awaitTermination(10,TimeUnit.SECONDS);
            }catch(InterruptedException e){
                log.error(e.getMessage());
                //强制关闭
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 打印线程池的状态
     *
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        //设置定时线程池任务，每隔一段时间执行一次
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
