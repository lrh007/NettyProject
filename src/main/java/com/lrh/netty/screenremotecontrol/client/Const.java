package com.lrh.netty.screenremotecontrol.client;

import java.awt.*;

/**
 * 常量类
 *
 * @Author lrh 2020/9/22 15:59
 */
public class Const {

    /**
     * 客户端自己的名称
     * @Author lrh 2020/9/22 9:55
     */
    public static String myClientName;

    /**
     * 服务器ip
     * @Author lrh 2020/9/22 17:30
     */
    public static final String SERVER_HOST = "127.0.0.1";
    /**
     * 服务器端口号
     * @Author lrh 2020/9/22 17:31
     */
    public static final int SERVER_PORT = 9527;
    /**   
     * 用来判断服务器是否连接成功
     * @Author lrh 2020/9/22 18:13
     */
    public static boolean CONNECT_SUCCESS = false;
    /**   
     * 服务器重新连接标识
     * @Author lrh 2020/9/23 10:34
     */
    public static boolean CONNECT_RETRY = false;
    /**   
     * 连接关闭标识
     * @Author lrh 2020/9/23 18:16
     */
    public static boolean CONNECT_CLOSE = false;
    
    /**   
     * 窗体宽度
     * @Author lrh 2020/9/22 16:00
     */
    public static final int FRAME_WIDTH = 600;
    /**   
     * 窗体高度
     * @Author lrh 2020/9/22 16:00
     */
    public static final int FRAME_HEIGHT = 400;
    /**   
     * 视图窗体宽度
     * @Author lrh 2020/9/23 15:02
     */
    public static final int VIEW_FRAME_WIDTH = 1000;
    /**   
     * 视图窗体高度
     * @Author lrh 2020/9/23 15:02
     */
    public static final int VIEW_FRAME_HEIGHT = 900;
    /**   
     * 获取指定大小的字体
     * @Author lrh 2020/9/22 16:02
     */
    public static Font FONT(int fontSize){
        return new Font(Font.SERIF,Font.PLAIN,fontSize);
    }
    /**
     * 给客户端发送，询问是否接受连接请求
     * @Author lrh 2020/9/23 10:09
     */
    public static final int STATUS_RECEIVE = 1;
    /**
     * 给客户端发送，发送数据请求
     * @Author lrh 2020/9/23 10:11
     */
    public static final int STATUS_SEND = 2;
    /**   
     * 客户端之间断开连接，不用再相互传输数据了
     * @Author lrh 2020/9/23 13:57
     */
    public static final int STATUS_CLOSE = 3;
    /**   
     * 拒绝连接
     * @Author lrh 2020/9/23 14:18
     */
    public static final int STATUS_REJECT = 4;
    /**   
     * 同意连接
     * @Author lrh 2020/9/23 14:19
     */
    public static final int STATUS_AGREE = 5;
    /***   
     * 找不到客户端
     * @Author lrh 2020/9/23 11:14
     */
    public static final int STATUS_NOT_FOUND = 404;
    

}
