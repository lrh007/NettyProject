package com.lrh.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 服务器处理器
 *
 * @Author lrh 2020/8/11 15:27
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter {
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        for(Channel ch: channels){
            if(ch != channel){
                ch.writeAndFlush(channel.remoteAddress()+": "+msg+"\n");
            }else{
                channel.writeAndFlush("me: "+msg+"\n");
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress()+"---上线");
        channels.writeAndFlush(channel.remoteAddress()+"----上线\n");
        channels.add(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress()+"----下线");
        channels.writeAndFlush(channel.remoteAddress()+"----下线\n");
        // channels中存放的都是Active状态的Channel，一旦某Channel的状态不再是Active，
        // channels会自动将其从集合中踢出，所以，下面的语句不用写
        // remove()方法的应用场景是，将一个Active状态的channel移出channels时使用
//        channels.remove(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
