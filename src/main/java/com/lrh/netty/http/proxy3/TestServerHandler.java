package com.lrh.netty.http.proxy3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @Author lrh 2020/9/10 13:38
 */
public class TestServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
//        System.out.println(buf.toString(CharsetUtil.UTF_8));
        ByteBuf data = Unpooled.copiedBuffer("hhhh",CharsetUtil.UTF_8);
//        ctx.writeAndFlush(data);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,data);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,data.readableBytes());
        ctx.writeAndFlush(response);
    }
}
