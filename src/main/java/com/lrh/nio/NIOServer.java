package com.lrh.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @Author lrh 2020/7/22 9:21
 */
public class NIOServer {
    private static final int BUF_SIZE = 1024;
    private static final int PORT = 7777;
    private static final int TIMEOUT = 3000;

    public static void main(String[] args) throws Exception {
//        client();
        selector();

//        UDPReceive();
//        UDPSend();
    }


    public static void selector(){
        ServerSocketChannel serverSocketChannel = null;
        Selector selector = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT)); //绑定端口号
            serverSocketChannel.configureBlocking(false); //设置成非阻塞模式
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while(true){
                //没有准备好链接，继续等待
                if(selector.select(TIMEOUT) == 0){
                    System.out.println("======");
                    continue;
                }
                //获取所有的key
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()){
                        handleAccept(selectionKey);
                    }
                    if(selectionKey.isReadable()){
                        handleRead(selectionKey);
                    }
                    if(selectionKey.isWritable()){
                        handleWrite(selectionKey);
                    }
                    if(selectionKey.isConnectable()){
                        System.out.println("isConnectable = true");
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(selector!=null){
                    selector.close();
                }
                if(serverSocketChannel!=null){
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 监听写入事件
     * @Author lrh 2020/7/22 10:51
     */
    private static void handleWrite(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }
        byteBuffer.compact(); //将已经写入的数据清空
    }

    /**
     * 监听读取操作
     * @Author lrh 2020/7/22 10:45
     */
    private static void handleRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        int bytesRead = socketChannel.read(byteBuffer); //将数据读取到byteBuffer中
        while(bytesRead > 0){
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()){
                System.out.println((char)byteBuffer.get());
            }
            byteBuffer.clear();
            bytesRead = socketChannel.read(byteBuffer);
        }
        if(bytesRead == -1){
            socketChannel.close();
        }

    }

    /**
     * 继续监听其他链接
     * @Author lrh 2020/7/22 10:40
     */
    private static void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false); //设置成非阻塞模式
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_READ,ByteBuffer.allocate(BUF_SIZE));//监听读取事件
    }

    /**
     * 发送udp数据
     * @Author lrh 2020/7/22 13:49
     */
    private static void UDPSend(){
        DatagramChannel datagramChannel = null;
        try {
            datagramChannel = DatagramChannel.open();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.clear();
            byteBuffer.put("I'm sender.".getBytes());
            byteBuffer.flip();
            int sendNum = datagramChannel.send(byteBuffer,new InetSocketAddress("127.0.0.1",7777));
            System.out.println("发送数据个数："+sendNum);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(datagramChannel!=null){
                try {
                    datagramChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 接受udp数据
     * @Author lrh 2020/7/22 13:49
     */
    private static void UDPReceive(){
        DatagramChannel datagramChannel = null;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.socket().bind(new InetSocketAddress(7777));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.clear();
            datagramChannel.receive(byteBuffer);
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()){
                System.out.print((char)byteBuffer.get());
            }
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(datagramChannel!=null){
                try {
                    datagramChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void client() throws IOException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",6666));
        socketChannel.configureBlocking(false);
        if(socketChannel.finishConnect()){
            int i=0;
            while(true){
                TimeUnit.SECONDS.sleep(1);
                String str = "I'm "+i+" client";
                buffer.clear(); //清空缓冲区
                buffer.put(str.getBytes());//将数据写入到缓冲区
                buffer.flip(); //将缓冲区反转，切换成读取模式
                while(buffer.hasRemaining()){
                    socketChannel.write(buffer);
                }
                i++;
            }
        }
        socketChannel.close();
    }

    public static void server() throws IOException{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(7777));
//        serverSocketChannel.configureBlocking(false);
        System.out.println("服务器启动。。。");
        while(true){
            System.out.println("等待客户端链接。。。");
            SocketChannel socketChannel = serverSocketChannel.accept();
            if(socketChannel != null){
                System.out.println("客户端链接成功");
                socketChannel.close();
            }
        }
    }
}
