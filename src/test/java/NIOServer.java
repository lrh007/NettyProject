import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author lrh 2020/7/23 17:21
 */
public class NIOServer {


    public static void main(String[] args) throws IOException {
        new NIOServer().testServer();
    }
//    @Test
    public void testServer() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功。。。");
        while(true){
            if(selector.select(2000) == 0){
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if(selectionKey.isAcceptable()){
                    System.out.println("接受连接。。。");
                    hadleAccept(selectionKey);
                }
                if(selectionKey.isReadable()){
                    System.out.println("读取数据。。。");
                    handleRead(selectionKey);
                }
                if(selectionKey.isWritable()){
                    System.out.println("写入数据。。。");
                    handleWrite(selectionKey);
                }
                if(selectionKey.isConnectable()){
                    System.out.println("客户端连接成功。。。");
                }
                iterator.remove();
            }
        }

    }

    private void handleWrite(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("helloworld!!!".getBytes());
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }
        byteBuffer.compact();
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if(read <0){
            socketChannel.close();
            selectionKey.cancel();
            return;
        }
        byteBuffer.flip();
        System.out.println(new String(byteBuffer.array()));
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_WRITE);

    }

    private void hadleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_READ);
    }
}
