package com.lrh.netty.http.proxy2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/9/2 11:20
 */
public class HttpProxyServerTargetHandler extends ChannelInboundHandlerAdapter {
    private final Channel inboundChannel;

    public HttpProxyServerTargetHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelFuture future = inboundChannel.writeAndFlush(msg);
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        HttpProxyServerHandler.closeOnFlush(ctx.channel());
    }
}
