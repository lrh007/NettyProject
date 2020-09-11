package com.lrh.netty.http.proxy2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

/**
 * @Author lrh 2020/9/2 10:57
 */
public class HttpProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    private final String remoteHost;
    private final int remotePort;
    private final SslContext sslContext;

    public HttpProxyServerInitializer(SslContext sslCtx,String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.sslContext = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if(sslContext != null){
            pipeline.addLast(sslContext.newHandler(socketChannel.alloc(),remoteHost,remotePort));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new LoggingHandler(LogLevel.ERROR))
                .addLast(new HttpProxyServerHandler(remoteHost,remotePort));
    }
}
