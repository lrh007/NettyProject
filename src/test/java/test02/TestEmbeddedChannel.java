package test02;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试使用Embeddedchannel测试handler
 *
 * @Author lrh 2020/7/30 16:15
 */
public class TestEmbeddedChannel {

    @Test
    public void testFramesDecoded(){
        ByteBuf buffer = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buffer.writeByte(i);
        }
        ByteBuf input = buffer.copy();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        Assert.assertTrue(channel.writeInbound(input));
        Assert.assertTrue(channel.finish());
        // read message
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertNull(channel.readInbound());
    }

    @Test
    public void testFramesDecoded2(){
        ByteBuf buffer = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buffer.writeByte(i);
        }
        ByteBuf input = buffer.copy();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        Assert.assertFalse(channel.writeInbound(input.readBytes(2)));
        Assert.assertTrue(channel.writeInbound(input.readBytes(7)));
        Assert.assertTrue(channel.finish());
        // read message
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertEquals(buffer.readBytes(3), channel.readInbound());
        Assert.assertNull(channel.readInbound());
    }

    @Test
    public void testEncoded(){
        ByteBuf buf = Unpooled.buffer();
        //初始化数据，并转换成负数
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }
        //创建EmbeddedChannel对象
        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
        //将buf数据写入出站EmbeddedChannel
        Assert.assertTrue(channel.writeOutbound(buf));
        //标示EmbeddedChannel完成
        Assert.assertTrue(channel.finish());

        //读取出站数据
        ByteBuf output= channel.readOutbound();
        for (int i = 1; i < 10; i++) {
            Assert.assertEquals(i,output.readInt()); //比较数据是否相等
        }
        Assert.assertFalse(output.isReadable());
        Assert.assertNull(channel.readOutbound());
    }



}
