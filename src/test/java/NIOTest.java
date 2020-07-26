import io.netty.buffer.ByteBuf;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @Author lrh 2020/7/23 9:59
 */
public class NIOTest {

    @Test
    public void testGenerator() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get("read.txt"), StandardOpenOption.APPEND);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        for (int i = 0; i < 500; i++) {
            byteBuffer.put(String.valueOf(new Random().nextInt(10)).getBytes());
            if(i%5==0){
                byteBuffer.put("\r\n".getBytes());
            }
        }
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){
            fileChannel.write(byteBuffer);
        }
        fileChannel.close();

    }
    @Test
    public void testReadChannel() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("read.txt");
        FileChannel channel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = channel.read(byteBuffer);
        System.out.println("读取字节数： "+read);
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){
            System.out.print((char)byteBuffer.get());
        }
        fileInputStream.close();
    }
    @Test
    public void testWriteChannel() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get("write.txt"), StandardOpenOption.WRITE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("你好呀".getBytes());
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
//        while(byteBuffer.hasRemaining()){
//            fileChannel.write(byteBuffer);
//        }
        fileChannel.close();
    }
    /**   
     * 文件复制速度慢
     * @Author lrh 2020/7/23 11:15
     */
    @Test
    public void testReadAndWrite() throws IOException {
        long startTime = System.currentTimeMillis();
        FileChannel readChannel = FileChannel.open(Paths.get("C:\\Users\\MACHENIKE\\Desktop\\1.txt"),StandardOpenOption.READ);
        FileChannel writeChannel = FileChannel.open(Paths.get("C:\\Users\\MACHENIKE\\Desktop\\2.txt"),StandardOpenOption.APPEND);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int i = readChannel.read(byteBuffer);
        System.out.println("总的字节数："+readChannel.size());
        while(i != -1){
//            System.out.println("读取子节数："+i);
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()){
                writeChannel.write(byteBuffer);
            }
            byteBuffer.compact();
            i = readChannel.read(byteBuffer);
        }

        writeChannel.close();
        readChannel.close();
        System.out.println("耗时： "+((System.currentTimeMillis()-startTime)/1000)+" s");
    }
    /**   
     * 复制文件速度最快
     * @Author lrh 2020/7/23 11:15
     */
    @Test
    public void testReadAndWrite2() throws IOException {
        long startTime = System.currentTimeMillis();
        FileChannel readChannel = FileChannel.open(Paths.get("C:\\Users\\MACHENIKE\\Desktop\\1.txt"),StandardOpenOption.READ);
        FileChannel writeChannel = FileChannel.open(Paths.get("C:\\Users\\MACHENIKE\\Desktop\\2.txt"),StandardOpenOption.APPEND);
        writeChannel.transferFrom(readChannel,0,readChannel.size());
        writeChannel.close();
        readChannel.close();
        System.out.println("耗时： "+((System.currentTimeMillis()-startTime)/1000)+" s");
    }

    @Test
    public void testScatter() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get("read.txt"), StandardOpenOption.READ);
        ByteBuffer headerBuffer = ByteBuffer.allocate(15);
        ByteBuffer bodyBuffer = ByteBuffer.allocate(1024);
        ByteBuffer[] byteBuffers = {headerBuffer,bodyBuffer};
        fileChannel.read(byteBuffers);
        headerBuffer.flip();
        while(headerBuffer.hasRemaining()){
            System.out.print((char)headerBuffer.get());
        }
        System.out.println("\r\n----------------");
        bodyBuffer.flip();
        while(bodyBuffer.hasRemaining()){
            System.out.print((char)bodyBuffer.get());
        }
        fileChannel.close();
    }
    @Test
    public void testGather() throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get("write.txt"), StandardOpenOption.APPEND);
        ByteBuffer headerBuffer = ByteBuffer.allocate(30);
        ByteBuffer bodyBuffer = ByteBuffer.allocate(1024);
        headerBuffer.put("this is header buffer".getBytes());
        bodyBuffer.put("this is body buffer!!!".getBytes());
        headerBuffer.flip();
        bodyBuffer.flip();
        ByteBuffer[] byteBuffers = {headerBuffer,bodyBuffer};
        fileChannel.write(byteBuffers);
        fileChannel.close();
    }

}
