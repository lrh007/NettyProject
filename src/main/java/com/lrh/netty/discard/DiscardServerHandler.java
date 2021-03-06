package com.lrh.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


/**
 * 丢弃服务器handler
 * @Author lrh 2020/7/22 16:38
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("丢弃收到的数据。。。");
        ByteBuf byteBuf = (ByteBuf)msg;
        System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
        byteBuf.release(); //丢弃收到的数据
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常就关闭链接
        cause.printStackTrace();
        ctx.close();
    }
}
