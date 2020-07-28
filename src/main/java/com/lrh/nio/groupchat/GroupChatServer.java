package com.lrh.nio.groupchat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

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
    private Map<String,SocketChannel> clientMap = new HashMap<>(); //保存所有的客户端

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
                    iterator.remove();
                    dealEvent(selectionKey);
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 处理事件
     * @Author lrh 2020/7/27 9:41
     */
    private void dealEvent(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isAcceptable()){
            ServerSocketChannel sc = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = sc.accept();
            socketChannel.configureBlocking(false); //设置非阻塞模式
            socketChannel.register(selector,SelectionKey.OP_READ);
            clientMap.put(socketChannel.getRemoteAddress().toString(),socketChannel);
            System.out.println(socketChannel.getRemoteAddress()+" 上线了。。。");
            socketChannel.write(ByteBuffer.wrap("========欢迎来到聊天室^v^=======".getBytes()));
        }
        if(selectionKey.isReadable()){
            handleRead(selectionKey);
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
                System.out.println("from 客户端: "+msg);
                //向其他客户端转发消息,去掉自己
                dispatchMsg(msg,socketChannel);
            }
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
     * 向客户端转发消息，需要排除自己
     * @Author lrh 2020/7/27 9:45
     */
    private void dispatchMsg(String msg,SocketChannel self){
        ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
        SocketChannel othersChannel = null;
        try{
            /*for(SelectionKey key : selector.keys()){
                SelectableChannel channel = key.channel();
                if(channel instanceof SocketChannel && channel!=socketChannel){
                    othersChannel = (SocketChannel) key.channel();
                    othersChannel.write(byteBuffer);
                }
            }*/
            Iterator<Map.Entry<String, SocketChannel>> iterator = clientMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, SocketChannel> map = iterator.next();
                othersChannel = map.getValue();
                if(othersChannel != self){
                    if(othersChannel.isOpen()){
                        while (byteBuffer.hasRemaining()){
                            othersChannel.write(byteBuffer);
                        }
                    }else{
                        iterator.remove();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GroupChatServer().listen();
    }
}
