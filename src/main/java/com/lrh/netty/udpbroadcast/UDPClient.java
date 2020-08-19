package com.lrh.netty.udpbroadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;

/**
 * 客户端
 *
 * @Author lrh 2020/8/18 10:15
 */
public class UDPClient {
    private static final int PORT = 7686;

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new UDPClientHandler())
                    .option(ChannelOption.SO_BROADCAST, true);
            Channel channel = bootstrap.bind(0).sync().channel();
            channel.writeAndFlush(
                    new DatagramPacket(Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8),
                            SocketUtils.socketAddress("255.255.255.255",PORT))).sync();
            if(!channel.closeFuture().await(5000)){
                System.out.println("QOTM request time out");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
