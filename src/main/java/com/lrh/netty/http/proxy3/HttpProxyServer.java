package com.lrh.netty.http.proxy3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 代理服务器
 *
 * @Author lrh 2020/9/9 15:11
 */
public class HttpProxyServer {
    /**
     * 和代理客户端内部通信端口
     * @Author lrh 2020/9/9 15:24
     */
    private static final int INSIDE_PORT = 9527;
    /**
     * 外部访问端口
     * @Author lrh 2020/9/9 15:24
     */
    private static final int OUTSIDE_PORT = 8843;

    public static void main(String[] args) {
        //监听和代理客户端通信端口
        NioEventLoopGroup bossGroup_inside = new NioEventLoopGroup();
        NioEventLoopGroup workGroup_inside = new NioEventLoopGroup();
        ServerBootstrap bootstrap_inside = new ServerBootstrap();
        //监听用户通过浏览器访问的端口
        NioEventLoopGroup bossGroup_outside = new NioEventLoopGroup();
        NioEventLoopGroup workGroup_outside = new NioEventLoopGroup();
        ServerBootstrap bootstrap_outside = new ServerBootstrap();

        bootstrap_inside.group(bossGroup_inside,workGroup_inside)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpProxyServerInSideInitializer());
        bootstrap_outside.group(bossGroup_outside,workGroup_outside)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpProxyServerOutSideInitializer());
        try {
            ChannelFuture clientFuture = bootstrap_inside.bind(INSIDE_PORT).sync();
            ChannelFuture browerFuture = bootstrap_outside.bind(OUTSIDE_PORT).sync();
            System.out.println("代理服务器启动成功，和代理客户端内部通信端口为："+INSIDE_PORT);
            System.out.println("代理服务器启动成功，外部访问端口为："+OUTSIDE_PORT);
            clientFuture.channel().closeFuture().sync();
            browerFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup_inside.shutdownGracefully();
            bossGroup_outside.shutdownGracefully();
            workGroup_inside.shutdownGracefully();
            workGroup_outside.shutdownGracefully();
        }
    }

}
