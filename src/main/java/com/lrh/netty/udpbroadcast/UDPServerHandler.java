package com.lrh.netty.udpbroadcast;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.Random;

/**
 * @Author lrh 2020/8/18 10:36
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Random random = new Random();

    // Quotes from Mohandas K. Gandhi:
    private static final String[] quotes = {
            "Where there is love there is life.",
            "First they ignore you, then they laugh at you, then they fight you, then you win.",
            "Be the change you want to see in the world.",
            "The weak can never forgive. Forgiveness is the attribute of the strong.",
    };

    private static String nextQuote(){
        int quoteId;
        synchronized (random){
            quoteId = random.nextInt(quotes.length);
        }
        return quotes[quoteId];
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        System.out.println(msg);
        if("QOTM?".equals(msg.content().toString(CharsetUtil.UTF_8))){
            ctx.writeAndFlush(
                    new DatagramPacket(Unpooled.copiedBuffer("QOTM:"+nextQuote(),CharsetUtil.UTF_8),msg.sender()));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
