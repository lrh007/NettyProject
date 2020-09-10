package com.lrh.netty.http.proxy2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * 代理服务器
 *
 * @Author lrh 2020/9/2 10:47
 */
public class HttpProxyServer {
    private static final boolean SSL = false;
    public static final int LOCAL_PORT = 8443;
    public static final String REMOTE_HOST = "127.0.0.1";
    public static final int REMOTE_PORT = 80;


    public static void main(String[] args) throws CertificateException, SSLException {
        final SslContext sslCtx;
        if(SSL){
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        }else{
            sslCtx = null;
        }
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpProxyServerInitializer(sslCtx,REMOTE_HOST,REMOTE_PORT));
        System.out.println("代理："+LOCAL_PORT+" TO "+REMOTE_HOST+":"+REMOTE_PORT);
        try {
            ChannelFuture future = b.bind(LOCAL_PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }


    }

}
