package com.lrh.netty.telnet;

import com.lrh.netty.udp.LogEvent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * 实现telnet服务器
 *
 * @Author lrh 2020/8/17 16:51
 */
public class TelnetServer {
    private int port;
    private final boolean SSL = System.getProperty("ssl") != null;
    private SslContext sslContext = null;
    public TelnetServer(int port,boolean enableSSL){
        this.port = port;
        if(enableSSL){
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContext = SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();
            } catch (CertificateException | SSLException e) {
                e.printStackTrace();
            }
        }
    }

    public void run(){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.ERROR))
                .childHandler(new TelnetServerInitializer(sslContext))
                .option(ChannelOption.SO_BACKLOG,128)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("服务器启动成功，监听端口： "+port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args) {
        new TelnetServer(80,false).run();
    }

}
