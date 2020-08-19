package com.lrh.netty.telnet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * @Author lrh 2020/8/18 9:23
 */
public class TelnetClientInitializer extends ChannelInitializer<SocketChannel> {
    private SslContext sslContext;

    public TelnetClientInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if(sslContext != null){
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc(),TelnetClient.HOST,TelnetClient.PORT));
        }
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new TelnetClientHandler());
    }
}
