package com.lrh.netty.http.proxy3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
        if(channel_outside != null && channel_outside.isActive()){
            channel_outside.writeAndFlush(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
