package com.lrh.netty.telnet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 客户端
 *
 * @Author lrh 2020/8/18 8:56
 */
public class TelnetClient {
    private static final boolean SSL = System.getProperty("ssl") != null;
    public static final String HOST = System.getProperty("host","127.0.0.1");
    public static final int PORT = Integer.parseInt(System.getProperty("port",SSL?"8992":"8023"));
    private SslContext sslContext = null;

    public TelnetClient(boolean enableSSL){
        if(enableSSL){
            try {
                sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
    }
    public void run(){
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new TelnetClientInitializer(sslContext))
                    .option(ChannelOption.SO_KEEPALIVE,true);
            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();
            ChannelFuture lastFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                String line = in.readLine();
                if(line == null){
                    break;
                }
                lastFuture = channel.writeAndFlush(line+"\r\n");
                if("bye".equals(line.toLowerCase())){
                    channel.closeFuture().sync();
                    break;
                }
            }
            if(lastFuture!=null){
                lastFuture.channel().closeFuture().sync();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new TelnetClient(false).run();
    }



}
