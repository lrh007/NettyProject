package com.lrh.netty.simplechat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 客户端
 *
 * @Author lrh 2020/7/28 15:21
 */
public class SimpleChatClient {
    public static void main(String[] args)  {
        run();
    }

    private static void run() {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try{
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleChatClientInitializer())
                    .option(ChannelOption.SO_KEEPALIVE,true);
            //连接服务器
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8080).sync();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                future.channel().writeAndFlush(in.readLine()+"\r\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
        }

    }
}
