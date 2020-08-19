package com.lrh.netty.udpbroadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * 服务器端
 *
 * @Author lrh 2020/8/18 10:28
 */
public class UDPServer {
    private static final int PORT = 7686;

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new UDPServerHandler())
                    .option(ChannelOption.SO_BROADCAST, true);
            bootstrap.bind(PORT).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
}
