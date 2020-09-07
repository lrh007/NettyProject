package com.lrh.netty.http.cors;

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
 * 这个示例服务器的目的是演示
 * 跨源资源共享 (CORS)在Netty。
 * 它没有像其他大多数示例那样的客户端，而是有一个html页面，用于在web浏览器中测试CORS支持。
 * < p >
 * CORS是在{@link HttpCorsServerInitializer}中配置的，通过更新配置，你可以尝试各种组合，比如使用一个特定的原点而不是通配符原点('*')。
 * < p >
 * 该文件{@code src/main/resources/cors/cors。包含一个非常基本的客户端示例，可以用来尝试dif
 * @Author lrh 2020/9/3 9:37
 */
public class HttpCorsServer {

    public static final boolean SSL = System.getProperty("ssl") != null;
    public static final int PORT = Integer.parseInt(System.getProperty("port",SSL?"8443":"8080"));

    public static void main(String[] args) throws CertificateException, SSLException {
        final SslContext sslCtx;
        if(SSL){
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();
        }else{
            sslCtx = null;
        }
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpCorsServerInitializer(sslCtx));
        try {
            ChannelFuture future = b.bind(PORT).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
