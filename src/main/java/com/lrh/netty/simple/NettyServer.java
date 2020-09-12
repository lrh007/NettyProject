package com.lrh.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //创建bossgroup 和workgroup
        /*
          * 1，创建两个线程组
          * 2，bossGroup 只是处理连接请求，真正的和客户端业务处理，会交给workGroup
          * 3, 两个都是无线循环
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        //创建服务器端的启动对象，配置参数
        ServerBootstrap bootstrap = new ServerBootstrap();
        //使用链式编程来进行设置参数
        bootstrap.group(bossGroup,workGroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class) //使用NioSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG,128)  //设置线程队列等待连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true) //设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道测试对象（匿名对象）
                        //给pipeLine设置处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(null);
                        }
                    }); //给我们的workGroup 的EventLoop 对应的管道设置处理器
        System.out.println("服务器准备好了。。。");
        //绑定了一个端口并且同步，生成了一个ChannelFuture对象
        ChannelFuture sync = bootstrap.bind(8080).sync();
        //对关闭通道进行监听
        sync.channel().closeFuture().sync();

    }
}
