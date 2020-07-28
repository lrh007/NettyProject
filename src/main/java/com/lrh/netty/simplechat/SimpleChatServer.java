package com.lrh.netty.simplechat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 简单聊天服务器
 *
 * @Author lrh 2020/7/28 14:42
 */
public class SimpleChatServer {
    public static void main(String[] args) throws InterruptedException {
        run();
    }

    private static void run() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SimpleChatServerInitializer())
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        //绑定端口
        ChannelFuture future = bootstrap.bind(8080).sync();
        //等待关闭事件
        future.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        System.out.println("SimpleChatServer 关闭了");

    }
}
