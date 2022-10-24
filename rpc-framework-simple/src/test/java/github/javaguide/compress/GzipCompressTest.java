package github.javaguide.compress;

import github.javaguide.compress.gzip.GzipCompress;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zhp
 * @date 2022-10-24 21:35
 */
public class GzipCompressTest {
    @Test
    public void testGzip(){
        byte [] data = new byte[1024*4];
        for(int i=0;i<1024*4;i++){
            data[i] = (byte) (new Random().nextInt(255)-128);
        }
        GzipCompress gzipCompress = new GzipCompress();
        byte[] compress = gzipCompress.compress(data);
        byte[] decompress = gzipCompress.decompress(compress);
        assertEquals(data.length,decompress.length);
      for(int i =0;i<1024*4;i++){
          if(decompress[i]!=data[i]){
              System.out.println("压缩出错，数据不一致");
              return;
          }
      }
        System.out.println("压缩解压正确，数据完全一致");
    }
}
