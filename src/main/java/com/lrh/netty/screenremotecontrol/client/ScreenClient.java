package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

/**
 * 客户端
 *
 * @Author lrh 2020/9/21 15:10
 */
public class ScreenClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9527;

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ScreenClientInitializer())
                .option(ChannelOption.SO_KEEPALIVE,true);
        try {
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            System.out.println("服务器连接成功。。。");
            Channel channel = future.channel();
            //输入数据的格式是，接收方名称:要发送的数据
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()){
                String s = scanner.nextLine();
                try{
                    String[] split = s.split(":");
                    ScreenData screenData = new ScreenData();
                    screenData.setSendName(ScreenClientHandler.myClientName);
                    screenData.setReceiveName(split[0]);
                    screenData.setContent(split[1]);
                    channel.writeAndFlush(screenData);
                }catch (Exception e){
                    System.out.println("输入不正确，请重新输入！");
                }

            }
//            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
}
