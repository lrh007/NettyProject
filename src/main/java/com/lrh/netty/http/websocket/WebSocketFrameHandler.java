package com.lrh.netty.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Locale;

/**
 * @Author lrh 2020/8/31 9:56
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if(frame instanceof TextWebSocketFrame){
            //发送返回的字符串
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
            String text = textWebSocketFrame.text();
            ctx.writeAndFlush(new TextWebSocketFrame(text.toLowerCase(Locale.US)));
        }else{
            String message = "不支持的框架式: "+frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
}
