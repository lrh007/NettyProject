package com.lrh.nio.groupchat;



import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

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
            generateUserName();//获取客户端名称
            System.out.println(userName+" 启动成功。。。");

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
                            try{
                                sc.finishConnect();
                                System.out.println("连接服务器成功。。。");
                                //单独开启一个线程用来输入数据
                                sendMsg(sc,selectionKey);
                            }catch (ConnectException e){
                                System.out.println("服务器故障，请重新连接。。。");
                            }
                        }
                        sc.register(selector,SelectionKey.OP_READ);
                    }
                    if(selectionKey.isReadable()){
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        try{
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            sc.read(byteBuffer);
                            byteBuffer.flip();
                            System.out.println(new String(byteBuffer.array()));
                            sc.register(selector,SelectionKey.OP_READ);
                        }catch (IOException e){
                            selectionKey.cancel();
                            sc.close();
                            System.out.println("服务器故障，请重新连接。。。");
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**   
     * 向服务器发送消息
     * @Author lrh 2020/7/27 10:13
     */
    public void sendMsg(final SocketChannel socketChannel,final SelectionKey selectionKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                try{
                    while(true){
                        byteBuffer.clear();
                        Scanner scanner = new Scanner(System.in);
                        String msg = scanner.nextLine();
                        msg = userName +" 说："+msg;
                        byteBuffer.put(msg.getBytes());
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                    }
                }catch (IOException e){
                    try {
                        selectionKey.cancel();
                        socketChannel.close();
                        System.out.println("服务器故障，请重新连接。。。");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }
    /**   
     * 生成客户端名称
     * @Author lrh 2020/7/27 16:27
     */
    private void generateUserName(){
        StringBuilder randomName = new StringBuilder();
        randomName.append("【");
        for (int i = 0; i < 5; i++) {
            randomName.append(new Random().nextInt(10)+1);
        }
        randomName.append("】");
        userName = randomName.toString();
    }

    public static void main(String[] args) {
        new GroupChatClient().listen();
    }
}
