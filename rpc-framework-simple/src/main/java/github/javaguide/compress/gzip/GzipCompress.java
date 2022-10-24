package github.javaguide.compress.gzip;

import github.javaguide.compress.Compress;

import javax.jws.Oneway;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author zhp
 * @date 2022-10-24 21:31
 * gzip方式的压缩数据方式
 */
public class GzipCompress implements Compress {

    //默认压缩大小 4kb
    private static final int BUFFER_SIZE = 1024*4;

    /**
     * gzip压缩数据
     * @param data
     * @return
     */
    @Override
    public byte[] compress(byte[] data) {
        if(data==null){
            throw new NullPointerException("传入数据data为空，无法压缩！");
        }
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(baos)){
            gos.write(data);
            gos.flush();
            gos.finish();
            return baos.toByteArray();
        }catch(IOException e){
            throw new RuntimeException("压缩过程出现错误",e);
        }
    }

    /**
     * gzip解压数据
     * @param data
     * @return
     */
    @Override
    public byte[] decompress(byte[] data) {
        if(data==null){
            throw new NullPointerException("传入数据data为空，无法解压！");
        }
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data))){
            int n ;
            byte [] buffer = new byte[BUFFER_SIZE];
            while((n=gis.read(buffer))>-1){
                baos.write(buffer,0,n);
            }
            return baos.toByteArray();
        }catch(IOException e){
            throw new RuntimeException("解压过程出现错误",e);
        }

    }
}
