package com.lrh.netty.websocket2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;

/**
 * @Author lrh 2020/12/24 16:14
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        //普通http请求
        if(o instanceof FullHttpRequest){
            handleHttpRequest(ctx,(FullHttpRequest)o);
        }else if(o instanceof WebSocketFrame){
            //websocket请求
            handleWebsocketRequest(ctx,(WebSocketFrame)o);
        }
    }
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        //http解码失败，返回异常信息
        if(!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))){
            sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        //构造握手响应返回
        final String url = "ws://localhost:8080/websocket";
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(url,null,false);
        handshaker = handshakerFactory.newHandshaker(req);
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(),req);
        }


    }
    private void handleWebsocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判读是否是关闭链路的指令
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
            return;
        }
        //判断是否是ping消息
        if(frame instanceof PingWebSocketFrame){
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }


    }
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse defaultFullHttpResponse) {
    }





    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
}
