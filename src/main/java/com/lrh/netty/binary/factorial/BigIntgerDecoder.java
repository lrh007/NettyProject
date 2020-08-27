package com.lrh.netty.binary.factorial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.math.BigInteger;
import java.util.List;

/** 自定义解码器
 * @Author lrh 2020/8/26 17:11
 */
public class BigIntgerDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //等待直到长度前缀可用
        if(byteBuf.readableBytes() < 5){
            return;
        }
        byteBuf.markReaderIndex();
        //检查这个神奇的数字。
        short magicNumber = byteBuf.readUnsignedByte();
        if(magicNumber != 'F'){
            byteBuf.resetReaderIndex();
            throw new CorruptedFrameException("Invalid magic number: " + magicNumber);
        }
        //等待，直到整个数据可用
        int dataLength = byteBuf.readInt();
        if(byteBuf.readableBytes() < dataLength){
            byteBuf.resetReaderIndex();
            return;
        }
        //将接收到的数据转换为新的BigInteger
        byte[] decoded = new byte[dataLength];
        byteBuf.readBytes(decoded);
        list.add(new BigInteger(decoded));

    }
}
