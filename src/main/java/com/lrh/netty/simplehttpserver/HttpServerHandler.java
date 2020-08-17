package com.lrh.netty.simplehttpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求处理器
 *
 * @Author lrh 2020/8/17 14:11
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        /*
         * 100 Continue含义
            HTTP客户端程序有一个实体的主体部分要发送给服务器，但希望在发送之前查看下服务器是否会接受这个实体，
            所以在发送实体之前先发送了一个携带100 Continue的Expect请求首部的请求。
            服务器在收到这样的请求后，应该用 100 Continue或一条错误码来进行响应
         * @Author lrh 2020/8/17 14:18
         */
        if(HttpUtil.is100ContinueExpected(req)){
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
        }
        String uri = req.uri();
        Map<String,String> responseMap = new HashMap<>();
        responseMap.put("method",req.method().name());
        responseMap.put("uri",uri);
        String msg = "<html>\n" +
                "<head>\n" +
                "\t<title>test netty HttpServer</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\t你请求的uri为: "+uri+"\n" +
                "</body>\n" +
                "</html>";
        System.out.println(ctx.channel().remoteAddress()+" : "+responseMap.toString());
        //创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        //设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=utf-8");
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=utf-8");
        //将html写到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
