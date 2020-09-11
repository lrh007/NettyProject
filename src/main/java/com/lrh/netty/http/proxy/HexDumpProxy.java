package com.lrh.netty.http.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 代理服务器
 *
 * @Author lrh 2020/9/2 9:07
 */
public class HexDumpProxy {
    //代理服务器的端口号
    public static final int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort","8443"));
    //被代理的远程服务器地址
    public static final String REMOTE_HOST = System.getProperty("remoteHost","127.0.0.1");
    //被代理的远程服务器端口号
    public static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort","80"));


    public static void main(String[] args) {
        System.err.println("代理："+LOCAL_PORT+" TO "+REMOTE_HOST+":"+REMOTE_PORT+"...");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HexDumpProxyInitializer(REMOTE_HOST,REMOTE_PORT))
                .childOption(ChannelOption.AUTO_READ,false);
        try {
            ChannelFuture future = bootstrap.bind(LOCAL_PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

}
