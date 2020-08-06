package com.lrh.netty.test.echoserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * 应答服务器客户端
 *
 * @Author lrh 2020/7/29 14:41
 */
public class EchoClient {
    private String host;
    private int port;
    public EchoClient(String host,int port){
        this.host = host;
        this.port = port;
    }
    public void start(){
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new EchoClientHandler());
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE,true);
        try {
            //连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //监听关闭事件
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new EchoClient("127.0.0.1",8080).start();
    }
}
