package com.lrh.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * bio服务端
 */
public class BIOServer {

    public static void main(String[] args) {
        System.out.println("服务器启动了");
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            //创建服务端，监听端口
            ServerSocket serverSocket = new ServerSocket(6666);
            while(true){
                System.out.println("等待连接。。。");
                final Socket socket = serverSocket.accept();
                System.out.println("连接上一个客户端。。。");
                executorService.execute(new Runnable() {
                    public void run() {
                        handler(socket);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取数据
     * @param socket
     */
    public static void handler(Socket socket){
        System.out.println("线程信息，id="+Thread.currentThread().getId()+",name="+Thread.currentThread().getName());
        byte[] bytes = new byte[1024];
        try {
            InputStream inputStream = socket.getInputStream();
            while(true){
                System.out.println("wait read。。。");
                int r = inputStream.read(bytes);
                if(r != -1){
                    System.out.println(new String(bytes));
                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
                System.out.println(Thread.currentThread().getId()+Thread.currentThread().getName()+"客户端关闭链接。。。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
