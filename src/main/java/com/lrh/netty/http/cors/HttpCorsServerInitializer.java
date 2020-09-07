package com.lrh.netty.http.cors;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

/**请参考{@link CorsConfig} javadocs获取所有的信息
     配置选项可用。
     下面是本例中讨论的一些配置:
     只支持一个特定的原点
     要支持单一原点而不是通配符，请使用以下方法:
     < >之前
     CorsConfig CorsConfig = CorsConfig. withorigin
     < / >之前
     允许从文件系统加载
     使服务器能够处理指定为“null”的原点
     当web浏览器从本地文件系统加载文件时，请使用以下方法:
     < >之前
     corsCo
 * @Author lrh 2020/9/3 10:21
 */
public class HttpCorsServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public HttpCorsServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
        if(sslCtx != null){
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpRequestDecoder());//request解码
        pipeline.addLast(new HttpResponseEncoder()); //response 编码
        pipeline.addLast(new HttpObjectAggregator(65536)); //消息聚合
        pipeline.addLast(new ChunkedWriteHandler());//大文件流传输
        pipeline.addLast(new CorsHandler(corsConfig)); //允许资源跨域请求
        pipeline.addLast(new OkResponseHandler()); //自定义业务处理器
    }
}
