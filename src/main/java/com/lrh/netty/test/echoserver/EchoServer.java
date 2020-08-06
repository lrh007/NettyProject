package com.lrh.netty.test.echoserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 应答服务器
 *
 * @Author lrh 2020/7/29 14:14
 */
public class EchoServer {
    private int port;
    public EchoServer(int port) {
        this.port = port;
    }
    public void start(){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();//处理所有的连接
        NioEventLoopGroup workGroup = new NioEventLoopGroup(); //处理具体的请求
        ServerBootstrap bootstrap = new ServerBootstrap(); //辅助启动类
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new EchoServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        try {
            //绑定端口号
            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("服务器启动成功，ip="+future.channel().localAddress()+",port="+port);
            //监听等待关闭事件
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new EchoServer(8080).start();
    }

}
