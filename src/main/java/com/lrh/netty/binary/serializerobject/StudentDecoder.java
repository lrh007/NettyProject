package com.lrh.netty.binary.serializerobject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * 自定义解码器
 *
 * @Author lrh 2020/8/26 14:53
 */
public class StudentDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.getBytes(0,bytes);
        //这里必须要读取数据，readIndex必须增加，在不读取数据的情况下可以使用skipBytes()方法跳过所有数据，并且可以使readIndex增加，
        // 否则就会报异常 did not read anything but decoded a message
        byteBuf.skipBytes(length);
        //从流中反序列化对象
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(inputStream);
        Object object = in.readObject();
        in.close();
        inputStream.close();
        list.add(object);

    }
}
