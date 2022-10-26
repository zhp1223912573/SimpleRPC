package github.javaguide.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhp
 * @date 2022-10-27 6:58
 * 存取和获取channel
 */
@Slf4j
public class ChannelProvider {

    //String--id地址 Channel--通道连接
    private final Map<String, Channel>channelMap;

    public ChannelProvider() {
        this.channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        //获取地址
        String address = inetSocketAddress.toString();
        if(channelMap.containsKey(address)){
            Channel channel = channelMap.get(address);
            if(channel!=null&&channel.isActive()){
                return channel;
            }else{
                channelMap.remove(address);
            }
        }
        return null;
    }


    public void set(Channel channel,InetSocketAddress inetSocketAddress){
        String address = inetSocketAddress.toString();
        channelMap.put(address,channel);
    }

    public void remove(InetSocketAddress inetSocketAddress){
        String address = inetSocketAddress.toString();
        channelMap.remove(address);
        log.info("ChannelMao size:[{}]", channelMap.size());
    }
}
