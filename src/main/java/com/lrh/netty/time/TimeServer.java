package com.lrh.netty.time;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 时间服务器
 *
 * @Author lrh 2020/7/28 13:37
 */
public class TimeServer {

    public static void main(String[] args) throws InterruptedException {
        run();
    }
    public static void run() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(); //处理连接
        NioEventLoopGroup workGroup = new NioEventLoopGroup(); //处理连接之后的分发的具体任务
        ServerBootstrap bootstrap = new ServerBootstrap(); //辅助启动类
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TimeServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        ChannelFuture sync = bootstrap.bind(8080).sync();
        sync.channel().closeFuture().sync();//监听关闭事件
        //关闭事件循环器
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
