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
    public static boolean CONNNECT_SUCCESS = false;
    
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
     * 获取指定大小的字体
     * @Author lrh 2020/9/22 16:02
     */
    public static Font FONT(int fontSize){
        return new Font(Font.SERIF,Font.PLAIN,fontSize);
    }
}
