package com.lrh.netty.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.util.Date;

/**
 * 服务端处理器
 *
 * @Author lrh 2020/8/17 17:43
 */
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is " + new Date() + " now.\r\n");
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String response;
        boolean close = false;
        if(s.isEmpty()){
            response = "Please type something.\r\n";
        }else if("bye".equals(s.toLowerCase())){
            response = "Have a good day!\r\n";
            close = true;
        }else{
            response = "Did you say '" + s + "'?\r\n";
        }
        ChannelFuture future = ctx.writeAndFlush(response);
        if(close){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
