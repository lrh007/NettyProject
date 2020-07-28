package com.lrh.netty.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 时间客户端
 *
 * @Author lrh 2020/7/28 13:57
 */
public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        run();
    }
    public static void run() throws InterruptedException {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TimeClientHandler());
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE,true);
        ChannelFuture sync = bootstrap.connect("127.0.0.1", 8080).sync();
        sync.channel().closeFuture().sync();//等待连接关闭
        workGroup.shutdownGracefully();
    }
}
