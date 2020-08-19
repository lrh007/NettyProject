package com.lrh.netty.http.snoop;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * http客户端
 *
 * @Author lrh 2020/8/19 9:49
 */
public class HttpSnoopClient {
    public static final String URL = System.getProperty("url", "http://127.0.0.1:8080/");

    public static void main(String[] args) throws URISyntaxException, SSLException {
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 8080;
            }
            if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            System.out.println("Only HTTP(S) is Support");
            return;
        }

        //判断是否使用https加密协议
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }
        //配置客户端
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpSnoopClientInitializer(sslCtx));
            Channel channel = bootstrap.connect(host, port).sync().channel();
            //准备http请求
            HttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
            request.headers().set(HttpHeaderNames.HOST,host);
            request.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING,HttpHeaderValues.GZIP);
            //设置一些示例cookie。
            request.headers().set(HttpHeaderNames.COOKIE,
                    ClientCookieEncoder.STRICT.encode(
                            new DefaultCookie("my-cookie","foo"),
                            new DefaultCookie("another-cookie","bar")));
            //发送http请求
            channel.writeAndFlush(request);
            //等待服务器关闭连接
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }
}
