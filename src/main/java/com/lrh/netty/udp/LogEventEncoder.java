package com.lrh.netty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 编码器
 *
 * @Author lrh 2020/8/11 10:15
 */
public class LogEventEncoder extends MessageToMessageEncoder<LogEvent> {

    private InetSocketAddress remoteAddress;

    public LogEventEncoder(InetSocketAddress remoteAddress){
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LogEvent logEvent, List<Object> list) throws Exception {
        ByteBuf buffer = channelHandlerContext.alloc().buffer();
        buffer.writeBytes(logEvent.getLogfile().getBytes(CharsetUtil.UTF_8));
        buffer.writeByte(LogEvent.SEPARATOR);
        buffer.writeBytes(logEvent.getMsg().getBytes(CharsetUtil.UTF_8));
        list.add(new DatagramPacket(buffer,remoteAddress));
    }
}
