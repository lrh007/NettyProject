package com.lrh.netty.simplechat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 简单聊天服务器处理器
 *
 * @Author lrh 2020/7/28 14:55
 */
public class SimpleChatServerHandler extends ChannelInboundHandlerAdapter {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    /**   
     * 上线通知
     * @Author lrh 2020/7/28 15:07
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        for (Channel channel :channels) {
            channel.writeAndFlush("【server】- "+inComing.remoteAddress()+" 加入\n");
        }
        channels.add(inComing);
    }
    /**   
     * 离线通知
     * @Author lrh 2020/7/28 15:07
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        for(Channel channel:channels){
            channel.writeAndFlush("【server】- "+inComing.remoteAddress()+" 离开\n");
        }
        channels.remove(inComing);
    }
    /**   
     * 转发消息给所有客户端
     * @Author lrh 2020/7/28 15:07
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel inComing = ctx.channel();
        for(Channel channel:channels){
            if(channel != inComing){
                channel.writeAndFlush("【"+inComing.remoteAddress()+"】"+msg+"\n");
            }else{
                channel.writeAndFlush("【you】"+msg+"\n");
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("SimpleChatClient: "+inComing.remoteAddress()+" 在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("SimpleChatClient: "+inComing.remoteAddress()+" 掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("SimpleChatClient: "+inComing.remoteAddress()+" 异常");
        //当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
