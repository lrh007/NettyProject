package com.lrh.netty.http.file2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 文件服务器
 *
 * @Author lrh 2020/12/23 14:13
 */
public class HttpFileServer {

    private static final String DEFAULT_URL = "E:\\";

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("http-decoder",new HttpRequestDecoder());//解码器
                        pipeline.addLast("http-aggregator",new HttpObjectAggregator(65536));//消息聚合
                        pipeline.addLast("http-encoder",new HttpResponseEncoder()); //编码器
                        pipeline.addLast("http-chunked",new ChunkedWriteHandler()); //大文件流输出
                        pipeline.addLast(new HttpFileServerHandler(DEFAULT_URL));
                    }
                });
        try {
            ChannelFuture f = b.bind(8080).sync();
            System.out.println("文件服务器启动成功，端口：8080 ，文件默认路径："+DEFAULT_URL);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

}
