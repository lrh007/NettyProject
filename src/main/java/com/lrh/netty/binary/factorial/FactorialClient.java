package com.lrh.netty.binary.factorial;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

/**
 * 客户端
 *
 * @Author lrh 2020/8/26 17:31
 */
public class FactorialClient {
    public static final boolean SSL = System.getProperty("ssl") != null;
    public static final String HOST = System.getProperty("host","127.0.0.1");
    public static final int PORT = Integer.parseInt(System.getProperty("port","8080"));
    public static final int COUNT = Integer.parseInt(System.getProperty("count","1000"));

    public static void main(String[] args) throws SSLException {
        final SslContext ssCtx;
        if(SSL){
            ssCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        }else{
            ssCtx = null;
        }
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new FactorialClientInitializer(ssCtx));
        try {
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }


    }
}
