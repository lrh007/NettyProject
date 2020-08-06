package test02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 求整数绝对值的编码处理器
 *
 * @Author lrh 2020/7/30 16:53
 */
public class AbsIntegerEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        while(byteBuf.readableBytes() >= 4){//整形是4个子节
            int abs = Math.abs(byteBuf.readInt());
            list.add(abs);
        }
    }
}
