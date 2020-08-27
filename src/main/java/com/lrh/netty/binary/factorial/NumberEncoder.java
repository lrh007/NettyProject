package com.lrh.netty.binary.factorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.math.BigInteger;

/** 自定义编码器
 * @Author lrh 2020/8/26 17:22
 */
public class NumberEncoder extends MessageToByteEncoder<Number> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Number msg, ByteBuf byteBuf) throws Exception {
        //为了更容易实现，首先转换为BigInteger
        BigInteger v;
        if(msg instanceof BigInteger){
            v = (BigInteger) msg;
        }else{
            v = new BigInteger(String.valueOf(msg));
        }
        //将数字转换为字节数组。
        byte[] bytes = v.toByteArray();
        int dateLength = bytes.length;
        //写出消息
        byteBuf.writeByte((byte)'F');
        byteBuf.writeInt(dateLength);
        byteBuf.writeBytes(bytes);
    }
}
