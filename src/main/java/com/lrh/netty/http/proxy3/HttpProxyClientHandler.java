package com.lrh.netty.http.proxy3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/9/9 17:07
 */
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 目标服务器的channel
     *
     * @Author lrh 2020/9/9 17:34
     */
    private Channel targetChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //代理服务器的channel
        final Channel proxyServerChannel = ctx.channel();
        //目标服务器地址
        final String targetHost = HttpProxyClient.TARGET_HOST;
        final int targetPort = HttpProxyClient.TARGET_PORT;

        //创建新的连接，连接到目标服务器,使用代理客户端的线程池
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new HttpProxyClientTargetHandler(proxyServerChannel));
        ChannelFuture future = b.connect(targetHost, targetPort);
        this.targetChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                    proxyServerChannel.read();
                }else{
                    proxyServerChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //向目标服务器转发消息
        if (targetChannel != null) {
            this.targetChannel.writeAndFlush(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
