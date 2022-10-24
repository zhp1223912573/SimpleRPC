package github.javaguide.compress;

import github.javaguide.extension.SPI;

/**
 * @author zhp
 * @date 2022-10-24 21:27
 * 数据压缩接口
 */
@SPI
public interface Compress {

    byte[] compress(byte[] data);

    byte[] decompress(byte[] data);
}
