package com.lrh.netty.http.snoop;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/8/19 10:41
 */
public class HttpSnoopClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            System.out.println("状态：" + response.status());
            System.out.println("版本：" + response.protocolVersion());
            System.out.println();
            //获取所有head中的参数
            if (!response.headers().isEmpty()) {
                for (String name : response.headers().names()) {
                    for (String value : response.headers().getAll(name)) {
                        System.err.println("HEADER: " + name + "=" + value);
                    }
                }
                System.out.println();
            }
            if (HttpUtil.isTransferEncodingChunked(response)) {
                System.err.println("CHUNKED CONTENT {");
            } else {
                System.err.println("CHUNKED {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            System.err.println(httpContent.content().toString(CharsetUtil.UTF_8));
            System.err.flush();
            if (httpContent instanceof LastHttpContent) {
                System.err.println("} END OF CONTENT");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
