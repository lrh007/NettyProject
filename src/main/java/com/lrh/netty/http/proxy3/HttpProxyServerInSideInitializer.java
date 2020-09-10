package com.lrh.netty.http.proxy3;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**服务器端内部访问的初始化器
 * @Author lrh 2020/9/9 15:51
 */
public class HttpProxyServerInSideInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.ERROR));
        pipeline.addLast(new HttpObjectAggregator(6553600));
        pipeline.addLast(new HttpProxyServerInSideHandler());
    }
}
