package com.lrh.netty.http.proxy3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/** 处理服务器端内部请求的业务逻辑
 * @Author lrh 2020/9/9 16:02
 */
public class HttpProxyServerInSideHandler extends ChannelInboundHandlerAdapter {
    /**
     * 内部通信的channel
     * @Author lrh 2020/9/9 16:21
     */
    public static Channel channel_inside;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("内部客户端上线："+ctx.channel().remoteAddress());
        channel_inside = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("代理服务器内部处理器收到的消息："+buf.toString(CharsetUtil.UTF_8));

        //获取外部通信的channel
        final Channel channel_outside = HttpProxyServerOutSideHandler.channel_outside;
        //向外部客户端转发消息
        if(channel_outside != null){
            ChannelFuture future = channel_outside.writeAndFlush(msg);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        ctx.channel().read();
                    }else{
                        channelFuture.channel().close();
                    }
                }
            });
        }else{
            System.out.println("代理服务器外部通信的channel为空！");
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
