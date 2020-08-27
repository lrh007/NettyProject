package com.lrh.netty.binary.factorial;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.ssl.SslContext;

/**
 * @Author lrh 2020/8/26 17:52
 */
public class FactorialClientInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;

    public FactorialClientInitializer(SslContext ssCtx) {
        this.sslCtx = ssCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if(sslCtx != null){
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc(),FactorialClient.HOST,FactorialClient.PORT));
        }
        //启用流压缩(如果没有必要，可以删除这两个)
        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        //添加自定义数字编解码器，
        pipeline.addLast(new BigIntgerDecoder());
        pipeline.addLast(new NumberEncoder());
        //添加自定义handler,处理业务逻辑
        pipeline.addLast(new FactorialClientHandler());

    }
}
