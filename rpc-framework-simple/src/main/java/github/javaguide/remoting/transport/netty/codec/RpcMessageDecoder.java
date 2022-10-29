package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;

/**
 * @author zhp
 * @date 2022-10-27 7:01
 *
 * custom protocol decoder
 *   <pre>
 *     0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *     +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *     |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *     +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *     |                                                                                                       |
 *     |                                         body                                                          |
 *     |                                                                                                       |
 *     |                                        ... ...                                                        |
 *     +-------------------------------------------------------------------------------------------------------+
 *   4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *   1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *   body（object类型数据）
 *
 *
 *   a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器
 *
 *   //
 *   https://www.jianshu.com/p/c6298073b6f0
 *
 *   继承LengthFieldBasedFrameDecoder解决TCP数据拆包和粘包的问题
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    // lengthFieldOffset: 包数据的长度偏移量。 魔数4字节，版本1字节，总共5字节。
    // lengthFieldLength: 包数据的长度信息长度。 4字节
    // lengthAdjustment: 读取的lengthFieldLegth加上当前字段达到包数据的末尾，读取的长度是整个包的长度，为了正确到达包的尾部应该-9，所以这里为-9
    // initialBytesToStrip: 包的有效数据的尾部依据确定，现在确定有效数据的首部，因为我们需要读取包数据开头的魔数和版本号进行校验，所以不跳过任意字节，所以这里为0
    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }


    /**
     * @param maxFrameLength      数据包最大长度，超出最大长度部分被抛弃
     * @param lengthFieldOffset   length字段的偏移量
     * @param lengthFieldLength  length字段的长度
     * @param lengthAdjustment   读取length加上当前字段，到达包有效数据的尾部
     * @param initialBytesToStrip 跳过包首部字节数，确定包有效数据的首部。如果需要全部包数据，该值为0
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //调用父类decode函数
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof  ByteBuf){
            //尝试解码
            ByteBuf frame = (ByteBuf) decoded;
            //存在可读数据
            if(frame.readableBytes()>=RpcConstants.TOTAL_LENGTH){
                try{
                    return decodeFrame(frame);
                }catch(Exception e){
                    log.error("解码数据帧错误！", e);
                    throw e;
                } finally {
                    //释放资源
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 将数据帧从二进制流转化为RpcMessage，后续handler会进行处理
     * @param in
     * @return
     */
    public Object decodeFrame(ByteBuf in){
        //检查魔数
        checkMagicNumber(in);
        //检查版本
        checkVersion(in);

        //数据长度
        int fullLength = in.readInt();
        //数据类型
        byte messageTyep = in.readByte();
        //序列化类型
        byte codec = in.readByte();
        //压缩类型
        byte compressType = in.readByte();
        //请求id
        int requestId = in.readInt();
        RpcMessage rpcMessage = new RpcMessage().builder()
                .codec(codec)
                .compress(compressType)
                .messageType(messageTyep)
                .requestId(requestId)
                .build();
        //检查是否为心跳请求
        if(messageTyep == RpcConstants.HEARTBEAT_REQUEST_TYPE){
          //  log.info("检查到发送心跳请求信息");
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if(messageTyep == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
         //   log.info("检查到发送心跳响应信息");
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        //读取数据，并对数据进行解压反序列化
        int dataLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(dataLength>0){
            byte [] data = new byte[dataLength];
            in.readBytes(data);
            //解压
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
           // log.info("解压类型为：[{}]",compressName);
            data = compress.decompress(data);
            //反序列化
            String codecName = SerializationTypeEnum.getName(codec);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
           // log.info("反序列化类型为：[{}]",codecName);
            if (messageTyep == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(data, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(data, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;

    }

    public void checkMagicNumber(ByteBuf in){
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte [] magicNumber = new byte[len];
        in.readBytes(magicNumber);
        for(int i=0;i<len;i++){
            if(magicNumber[i]!=RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("未知魔数: " + Arrays.toString(magicNumber));
            }
        }
    }

    public void checkVersion(ByteBuf in){
        byte version = in.readByte();
        if(version!=RpcConstants.VERSION){
            throw new RuntimeException("版本类型不匹配！");
        }
    }



}
