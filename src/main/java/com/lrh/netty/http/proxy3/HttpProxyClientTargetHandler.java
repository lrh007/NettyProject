package com.lrh.netty.http.proxy3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author lrh 2020/9/9 17:51
 */
public class HttpProxyClientTargetHandler extends ChannelInboundHandlerAdapter {
    /**   
     * 代理服务器的channel
     * @Author lrh 2020/9/9 17:52
     */
    private final Channel proxyServerChannel;
    
    public HttpProxyClientTargetHandler(Channel proxyServerChannel) {
        this.proxyServerChannel = proxyServerChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //向代理服务器转发消息
        if(proxyServerChannel != null){
            proxyServerChannel.writeAndFlush(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
