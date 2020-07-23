import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @Author lrh 2020/7/23 15:55
 */
public class NIOClient {

    @Test
    public void testClient() throws IOException {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        while(true){
            if(selector.select(2000) == 0){
                System.out.println("客户端尝试连接服务器。。。");
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if(selectionKey.isConnectable()){
                    System.out.println("客户端连接服务器成功。。。");
                    handleConnect(selectionKey);
                }
                if(selectionKey.isReadable()){
                    System.out.println("客户端开始读取数据。。。");
                    handleRead(selectionKey);
                }
                if(selectionKey.isWritable()){
                    System.out.println("客户端开始向服务发送数据。。。");
                    handleWrite(selectionKey);
                }
                iterator.remove();
            }
        }
    }

    private void handleWrite(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.clear();
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        byteBuffer.put(msg.getBytes());
        if(msg.length()>0){
            byteBuffer.flip();
        }
        socketChannel.write(byteBuffer);
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if(read > 0){
            byteBuffer.flip();
            System.out.println("服务器说："+new String(byteBuffer.array()));
            socketChannel.register(selectionKey.selector(),SelectionKey.OP_WRITE);
        }
    }

    private void handleConnect(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        if (socketChannel.isConnectionPending()) {
            socketChannel.finishConnect();
            System.out.println("服务器连接成功。。。");
            socketChannel.register(selectionKey.selector(),SelectionKey.OP_WRITE);
        }
    }
}
