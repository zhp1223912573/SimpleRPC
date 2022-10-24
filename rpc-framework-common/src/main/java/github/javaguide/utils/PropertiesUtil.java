package github.javaguide.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author zhp
 * @date 2022-10-24 19:49
 * 配置文件工具类
 */
@Slf4j
public class PropertiesUtil {

    /**
     * 读取指定文件的内容
     * @param fileName
     * @return
     */
    public static Properties readPropertiesFile(String fileName){
        //获取文件路径
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfig = "";
        if(url!=null){
            rpcConfig = url.getPath()+fileName;
        }
        //读取配置文件
        Properties properties = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(rpcConfig), StandardCharsets.UTF_8)){
            properties = new Properties();
            properties.load(inputStreamReader);
        }catch(IOException e){
            log.error(e.getMessage());
        }
        return properties;
    }
}
