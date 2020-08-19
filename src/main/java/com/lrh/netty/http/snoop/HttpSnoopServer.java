package com.lrh.netty.http.snoop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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
 * 返回接收到的HTTP请求的内容的HTTP服务器
 *
 * @Author lrh 2020/8/19 11:05
 */
public class HttpSnoopServer {

    public static final boolean SSL = System.getProperty("ssl") != null;
    public static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "443" : "8080"));

    public static void main(String[] args) throws CertificateException, SSLException {
        //配置SSL。
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        //配置服务器
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpSnoopServerInitializer(sslCtx));
            Channel channel = bootstrap.bind(PORT).sync().channel();
            System.err.println("打开web浏览器,并导航到 "+(SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
