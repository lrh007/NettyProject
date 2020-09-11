package com.lrh.netty.http.proxy3;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author lrh 2020/9/11 10:16
 */
public class TestClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端上线,isActive="+ctx.channel().isActive()+",isOpen="+ctx.channel().isOpen()+",isWritable="+ctx.channel().isWritable());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("读取数据,isActive="+ctx.channel().isActive()+",isOpen="+ctx.channel().isOpen()+",isWritable="+ctx.channel().isWritable());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端下线,isActive="+ctx.channel().isActive()+",isOpen="+ctx.channel().isOpen()+",isWritable="+ctx.channel().isWritable());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
