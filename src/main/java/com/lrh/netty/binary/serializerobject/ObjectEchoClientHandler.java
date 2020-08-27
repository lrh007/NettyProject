package com.lrh.netty.binary.serializerobject;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author lrh 2020/8/26 14:13
 */
public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {
    private final List<Integer> firstMessage;
    private final List<Student> allList;
    public ObjectEchoClientHandler() {
        this.firstMessage = new ArrayList<>();
        for (int i = 0; i < ObjectEchoClient.SIZE; i++) {
            firstMessage.add(i);
        }
        this.allList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            allList.add(new Student("张三"+i,(new Random().nextInt(10)+10),new Random().nextInt(2)));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(firstMessage);
        ctx.writeAndFlush(allList);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
        ctx.close().sync();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
