package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhp
 * @date 2022-10-27 7:00
 *
 *   custom protocol decoder
 *     <pre>
 *       0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *       +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *       |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *       +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *       |                                                                                                       |
 *       |                                         body                                                          |
 *       |                                                                                                       |
 *       |                                        ... ...                                                        |
 *       +-------------------------------------------------------------------------------------------------------+
 *     4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *     1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *     body（object类型数据）
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    //做RpcMessage的requestId生成
    private static final AtomicInteger ATIOMIC_INTEGER = new AtomicInteger(0);


    /**
     *  依据RpcMessage数据格式向out写入对应数据
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        try{
            //魔数（4字节）
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            //版本（1字节）
            out.writeByte(RpcConstants.VERSION);
            //留出4字节给数据长度，等到其他数据都填写完成再添加
            out.writerIndex(out.writerIndex()+4);
            //RpcMessage数据类型（1字节）
            out.writeByte(msg.getMessageType());
            //compress压缩类型（1字节）
            out.writeByte(msg.getCompress());
            //codec序列化类型(1字节）
            out.writeByte(msg.getCodec());
            //requstId请求id（4字节）使用AtomicInteger生成
            out.writeInt(ATIOMIC_INTEGER.getAndIncrement());
            //data Rpc请求的具体数据 根据数据类型
            //获取数据进行序列化与压缩
            byte [] bodydata = null;
            //目前数据长度
            int fullLength =RpcConstants.HEAD_LENGTH;
            //如果数据不是心跳数据，对数据进行序列化与压缩
            if(msg.getMessageType()!=RpcConstants.HEARTBEAT_REQUEST_TYPE
                    &&msg.getMessageType()!=RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                //序列化器类型
                String serializerName = SerializationTypeEnum.getName(msg.getCodec());
                log.info("序列化类型为 [{}]",serializerName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
                //对msg中的data进行序列化
                bodydata = serializer.serialize(msg.getData());
                //压缩类型
                String compressName = CompressTypeEnum.getName(msg.getCodec());
                log.info("压缩类型为 [{}]",compressName);
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                //压缩数据
                bodydata = compress.compress(bodydata);
                //获取完成数据帧长度
                fullLength = fullLength+bodydata.length;
            }
            //写入buf
            if(bodydata!=null){
                out.writeBytes(bodydata);
            }

            //回到fullLength位置，填充数据长度
            //记录当前位置，后续需要回到该位置
            int index = out.writerIndex();
            //跳到fullLength位置
            out.writerIndex(index-fullLength+RpcConstants.MAGIC_NUMBER.length+1);
            //写入长度
            out.writeInt(fullLength);
            //更新位置
            out.writerIndex(index);
        }catch(Exception e){
            log.error("RpcMessage编码错误");
        }
    }
}
