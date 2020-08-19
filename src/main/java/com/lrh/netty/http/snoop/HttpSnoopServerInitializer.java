package com.lrh.netty.http.snoop;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * @Author lrh 2020/8/19 11:21
 */
public class HttpSnoopServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public HttpSnoopServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if(sslCtx != null){
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        //如果不需要自动内容压缩，请删除以下行。
        pipeline.addLast(new HttpContentCompressor());
        //如果您不想处理HttpContents，请取消下面一行的注释,消息聚合，解决分片数据
//        pipeline.addLast(new HttpObjectAggregator(512 * 1024));
        pipeline.addLast(new HttpSnoopServerHandler());
    }
}
