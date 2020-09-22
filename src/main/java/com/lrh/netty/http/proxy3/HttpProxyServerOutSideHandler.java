package com.lrh.netty.http.proxy3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

/** 处理服务器端外部请求的业务逻辑
 * @Author lrh 2020/9/9 16:01
 */
public class HttpProxyServerOutSideHandler extends ChannelInboundHandlerAdapter {
    /**   
     * 外部通信的channel
     * @Author lrh 2020/9/9 16:22
     */
    public static Channel channel_outside;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("外部客户端上线："+ctx.channel().remoteAddress());
        channel_outside = ctx.channel();
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
//        System.out.println("代理服务器外部处理器收到的消息："+buf.toString(CharsetUtil.UTF_8));
        System.out.println("代理服务器外部处理器收到的消息");
        //获取内部通信的channel
        final Channel channel_inside = HttpProxyServerInSideHandler.channel_inside;
        //向内部通信的客户端转发消息
        if(channel_inside != null ){
            ChannelFuture future = channel_inside.writeAndFlush(msg);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        ctx.channel().read();
                    }else{
                        ctx.channel().close();
                    }
                }
            });
        }else{
            System.out.println("代理服务器内部通信的channel为空！");
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(HttpProxyServerInSideHandler.channel_inside != null && HttpProxyServerInSideHandler.channel_inside.isActive()){
            HttpProxyServerInSideHandler.channel_inside.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
