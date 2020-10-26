package com.lrh.netty.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 客户端处理器
 */
public class ProtoBufClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //使用protobuf发送一个对象到服务器
        StudentPOJO.Studnet.Builder builder = StudentPOJO.Studnet.newBuilder();
        builder.setId(25);
        builder.setName("啦啦啦");
        ctx.writeAndFlush(builder);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器返回："+((ByteBuf)msg).toString(CharsetUtil.UTF_8));
    }
}
