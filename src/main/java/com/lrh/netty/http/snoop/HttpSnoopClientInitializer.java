package com.lrh.netty.http.snoop;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;


/**
 * @Author lrh 2020/8/19 10:33
 */
public class HttpSnoopClientInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    public HttpSnoopClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //如果需要，启用HTTPS。
        if(sslCtx != null){
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpClientCodec());
        //如果不需要自动内容解压，请删除以下行。
        pipeline.addLast(new HttpContentDecompressor());
        //如果您不想处理HttpContents，请取消下面一行的注释,消息聚合，解决分片数据
//        pipeline.addLast(new HttpObjectAggregator(512 * 1024));
        pipeline.addLast(new HttpSnoopClientHandler());

    }
}
