package com.lrh.netty.http.proxy3;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * @Author lrh 2020/9/9 17:06
 */
public class HttpProxyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpObjectAggregator(6553600));
        pipeline.addLast(new HttpProxyClientHandler());
    }
}
