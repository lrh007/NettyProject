package com.lrh.netty.http.proxy3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 代理客户端
 *
 * @Author lrh 2020/9/9 16:52
 */
public class HttpProxyClient {
    /**
     * 代理服务器端口
     * @Author lrh 2020/9/9 16:56
     */
    public static final int PROXY_PORT = 9527;
    /**
     * 代理服务器ip
     * @Author lrh 2020/9/9 16:56
     */
    public static final String PROXY_HOST = "127.0.0.1";
    /**
     * 目标服务器端口
     * @Author lrh 2020/9/9 16:57
     */
    public static final int TARGET_PORT = 80;
    /**
     * 目标服务器ip
     * @Author lrh 2020/9/9 16:57
     */
    public static final String TARGET_HOST = "127.0.0.1";

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new HttpProxyClientInitializer())
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.AUTO_READ,false);
        try {
            ChannelFuture future = bootstrap.connect(PROXY_HOST, PROXY_PORT).sync();
            System.out.println("代理客户端启动成功，连接代理服务器成功，代理服务器地址："+PROXY_HOST+":"+PROXY_PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

}
