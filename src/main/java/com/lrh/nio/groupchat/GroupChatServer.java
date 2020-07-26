package com.lrh.nio.groupchat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * nio群聊服务器端
 */
public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final int PORT = 8080;
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024); //要发送的数据
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024); //接收到的数据
    private SocketChannel self; //每次需要排除的对象
    private List<SocketChannel> list = new ArrayList<>();

    public GroupChatServer() {
        try {
            selector = Selector.open();  //得到选择器
            serverSocketChannel = ServerSocketChannel.open(); //得到serverSocketChannel
            serverSocketChannel.configureBlocking(false); //设置非阻塞模式
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT)); //绑定端口号
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //将serverSocketChannel注册到selector
            System.out.println("服务器启动成功，监听端口："+PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听客户端连接
     */
    public void listen(){
        try {
            while(true){
                if(selector.select(1000) == 0){
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        ServerSocketChannel sc = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = sc.accept();
                        socketChannel.configureBlocking(false); //设置非阻塞模式
                        socketChannel.register(selector,SelectionKey.OP_READ);
                        System.out.println(socketChannel.getRemoteAddress()+" 上线了。。。");
                        list.add(socketChannel);
                    }
                    if(selectionKey.isReadable()){
                        handleRead(selectionKey);
                    }
                    if(selectionKey.isWritable()){
//                       SocketChannel sc = (SocketChannel) selectionKey.channel();
                       senInfoToOtherClients(new String(receiveBuffer.array()),self);
//                       sc.register(selector,SelectionKey.OP_READ);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取客户端数据
     * @param selectionKey
     */
    private void handleRead(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            int read = socketChannel.read(byteBuffer);
            //读取到的数据不为空，转换成字符串输出
            if(read > 0){
                String msg = new String(byteBuffer.array());
                System.out.println("from 客户端"+msg);
                //向其他客户端转发消息,去掉自己
//                senInfoToOtherClients(msg,socketChannel);
                receiveBuffer.clear();
                receiveBuffer.put(byteBuffer.array());
                self = socketChannel;
            }
            socketChannel.register(selector,SelectionKey.OP_WRITE);
        } catch (IOException e) {
            try {
                //发送读取异常，可能是客户端关闭了
                System.out.println(socketChannel.getRemoteAddress()+" 离线了。。。");
                //取消注册
                selectionKey.cancel();
                //关闭通道
                socketChannel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 向其他客户端转发消息，排除自己
     * @param msg
     * @param self
     */
    private void senInfoToOtherClients(String msg,SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中。。。");
        ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
        //遍历所有注册到selector上的SocketChannel,并排除自己
        for (SocketChannel sc : list) {
            //排除自己
//            if(sc != self){
                //将消息转换成ByteBuffer
                //将消息写入到channel中
                sc.write(byteBuffer);
                sc.register(selector,SelectionKey.OP_READ);
//            }
        }
    }

    public static void main(String[] args) {
        new GroupChatServer().listen();
    }
}
