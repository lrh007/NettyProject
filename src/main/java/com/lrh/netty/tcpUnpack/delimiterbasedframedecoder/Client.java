package com.lrh.netty.tcpUnpack.delimiterbasedframedecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 客户端
 *
 * @Author lrh 2020/12/21 17:02
 */
public class Client {

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        ChannelPipeline pipeline = sc.pipeline();
                        ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new ClientHandler());
                    }
                });
        try {
            ChannelFuture f = b.connect("127.0.0.1", 8080).sync();//同步等待成功
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }


    }
}
