package com.lrh.nio.groupchat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * 群聊系统客户端
 */
public class GroupChatClient {

    private static final String HOST = "127.0.0.1"; //服务器ip
    private static final int PORT = 8080; //服务器端口
    private Selector selector;
    private String userName; //客户端名称

    public GroupChatClient() {
        try {
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false); //设置非阻塞模式
            socketChannel.register(selector, SelectionKey.OP_CONNECT); //将socketChannel注册到selector
            socketChannel.connect(new InetSocketAddress(HOST,PORT)); //连接服务器
            userName = String.valueOf(new Random().nextInt(10)); //获取客户端名称
            System.out.println(userName+" is ok。。。");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(){
        try{
            while(true){
                if(selector.select(1000) == 0){
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if(selectionKey.isConnectable()){
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        if(sc.isConnectionPending()){
                            sc.finishConnect();
                            sc.register(selector,SelectionKey.OP_WRITE);
                        }
                    }
                    if(selectionKey.isReadable()){
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        sc.read(byteBuffer);
                        byteBuffer.flip();
                        System.out.println(new String(byteBuffer.array()));
                        sc.register(selector,SelectionKey.OP_WRITE);
                    }
                    if(selectionKey.isWritable()){
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        Scanner scanner = new Scanner(System.in);
                        String msg = scanner.nextLine();
                        msg = userName +" 说："+msg;
                        byteBuffer.put(msg.getBytes());
                        byteBuffer.flip();
                        sc.write(byteBuffer);
                        sc.register(selector,SelectionKey.OP_READ);
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        GroupChatClient chatClient = new GroupChatClient();
        chatClient.listen();
    }
}
