package com.lrh.netty.simplehttpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 自定义初始化器
 *
 * @Author lrh 2020/8/17 14:05
 */
public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec());//http编码，解码器
        pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024)); //http消息聚合器
        pipeline.addLast(new HttpServerHandler());//请求处理器
    }
}
