package com.lrh.netty.binary.serializerobject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * 自定义编码器
 *
 * @Author lrh 2020/8/26 14:36
 */
public class StudentEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf byteBuf) throws Exception {
        //将对象序列化保存到流中
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(object);
        //从流中获取对象的子节数据
        byteBuf.writeBytes(outputStream.toByteArray());
    }
}
