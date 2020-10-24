package com.lrh.netty.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 服务器端处理器
 */
public class ProtoBufServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        StudentPOJO.Studnet studnet = (StudentPOJO.Studnet) msg;
        System.out.println("服务器收到的消息：id="+studnet.getId()+",name="+studnet.getName());
        ctx.writeAndFlush(Unpooled.copiedBuffer("这里是服务器，欢迎你！",CharsetUtil.UTF_8));
    }
}
