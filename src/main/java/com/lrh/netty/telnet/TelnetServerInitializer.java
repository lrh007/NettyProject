package com.lrh.netty.telnet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;


/**
 * 初始化
 *
 * @Author lrh 2020/8/17 17:37
 */
public class TelnetServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslContext;

    public TelnetServerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }


    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        ChannelPipeline pipeline = sc.pipeline();
        if(sslContext != null){
            pipeline.addLast(sslContext.newHandler(sc.alloc()));
        }
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192));
        pipeline.addLast(new TelnetServerHandler());
    }
}
