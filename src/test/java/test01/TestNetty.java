package test01;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.NettyRuntime;
import org.junit.Test;

import java.util.Iterator;

/**
 * @Author lrh 2020/7/30 9:45
 */
public class TestNetty {

    @Test
    public void testCompositeByteBuf(){
        //通过字节数组池创符合数组
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
        ByteBuf headBuf = Unpooled.buffer(8);
        ByteBuf direcBuf = Unpooled.directBuffer(16);
        //添加ByteBuf到CompositeByteBuf
        compositeByteBuf.addComponents(headBuf,direcBuf);
        //删除第一个数组
        compositeByteBuf.removeComponent(0);
        Iterator<ByteBuf> iterator = compositeByteBuf.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next().toString());
        }
        //使用数组访问数据
        if(!compositeByteBuf.hasArray()){
            int len = compositeByteBuf.readableBytes();
            byte[] arr = new byte[len];
            compositeByteBuf.getBytes(0,arr);
        }
    }

    @Test
    public void testReadAndWrite(){
        ByteBuf byteBuf = Unpooled.buffer(16);
        for (int i = 0; i < 16; i++) {
            byteBuf.writeByte(i+1);
        }
        for (int i = 0; i < byteBuf.capacity(); i++) {
            System.out.println(byteBuf.getByte(i));
        }
        System.out.println(ByteBufUtil.hexDump(byteBuf));
    }
    @Test
    public void test01(){
        ByteBuf buf = Unpooled.copiedBuffer("Netty in action rocks!", CharsetUtil.UTF_8);
        ByteBuf slice = buf.slice(0,14);
        ByteBuf copy = buf.copy(0,14);
        System.out.println(buf.toString(CharsetUtil.UTF_8));
        System.out.println(slice.toString(CharsetUtil.UTF_8));
        System.out.println(copy.toString(CharsetUtil.UTF_8));
    }
    @Test
    public void test03(){
        System.out.println(NettyRuntime.availableProcessors());
    }

    @Test
    public void test04(){
        ByteBuf buf = Unpooled.copiedBuffer("hello world!",CharsetUtil.UTF_8);
        String s = ByteBufUtil.hexDump(buf);
        System.out.println(s);
    }

}
