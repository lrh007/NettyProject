package com.lrh.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 客户端
 *
 * @Author lrh 2020/8/11 15:39
 */
public class ChatClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        run();
    }
    public static void run(){
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(2048));
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new StringEncoder());
                        socketChannel.pipeline().addLast(new ChatClientHandler());
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            System.out.println("客户端连接服务器成功，服务器地址： "+HOST+":"+PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
            while(true){
                future.channel().writeAndFlush(in.readLine()+"\r\n");
            }

//            future.channel().closeFuture().sync();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
