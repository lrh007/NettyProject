package com.lrh.netty.binary.serializerobject;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

/**
 * 二进制客户端
 *
 * @Author lrh 2020/8/26 14:03
 */
public class ObjectEchoClient {
    public static final boolean SSL = System.getProperty("ssl") != null;
    public static final String HOST = System.getProperty("host","127.0.0.1");
    public static final int PORT = Integer.parseInt(System.getProperty("port","8080"));
    public static final int SIZE = Integer.parseInt(System.getProperty("size","256"));

    public static void main(String[] args) throws SSLException {
        final SslContext sslCtx;
        if(SSL){
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        }else {
            sslCtx = null;
        }
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if(sslCtx != null){
                            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
                        }
//                        pipeline.addLast(new ObjectEncoder());
//                        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new StudentEncoder());
                        pipeline.addLast(new StudentDecoder());
                        pipeline.addLast(new ObjectEchoClientHandler());
                    }
                });
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
