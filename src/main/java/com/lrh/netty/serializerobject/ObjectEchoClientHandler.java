package com.lrh.netty.serializerobject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lrh 2020/8/18 13:52
 */
public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {
    private final List<Integer> firstMessage;

    public ObjectEchoClientHandler(){
        firstMessage = new ArrayList<>(ObjectEchoClient.SIZE);
        for (int i = 0; i < ObjectEchoClient.SIZE; i++) {
            firstMessage.add(i);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
