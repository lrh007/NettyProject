package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.View;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static java.awt.Frame.ICONIFIED;

/** 业务处理
 * @Author lrh 2020/9/21 15:15
 */
public class ScreenClientHandler extends SimpleChannelInboundHandler<ScreenData> {

    /**   
     * 用来判断MainFrame是否已经最小化
     * @Author lrh 2020/9/24 9:23
     */
    private static boolean show = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ScreenData screenData) throws Exception {
        //获取服务器分配的客户端名称
        if(screenData.getSendName() == null && screenData.getReceiveName() == null){
            Const.myClientName = screenData.getContent();
            System.out.println("服务器分配的名称： "+Const.myClientName);
        }else{
            if(screenData.getSendName().equalsIgnoreCase(Const.myClientName) && Const.STATUS_NOT_FOUND == screenData.getStatus()){
//                System.out.println("自己说： "+screenData.getContent());
                JOptionPane.showMessageDialog(null,screenData.getReceiveName()+"不在线");
            }else{
                System.out.println(screenData.getSendName()+"说： "+screenData.getContent()+"，状态="+screenData.getStatus());
                handler(ctx,screenData);
            }
        }

    }
    /**
     * 处理业务逻辑
     * @Author lrh 2020/9/23 14:07
     */
    private void handler(ChannelHandlerContext ctx,ScreenData screenData){
        //请求连接
        if(screenData.getStatus() == Const.STATUS_RECEIVE){
            int i = JOptionPane.showConfirmDialog(null, screenData.getSendName() + "请求连接本机，是否同意？","提示",JOptionPane.YES_NO_OPTION);
            //同意连接，开始发送数据
            if(i == JOptionPane.YES_OPTION){
//                ctx.writeAndFlush(new ScreenData(screenData.getReceiveName(),screenData.getSendName(),Const.STATUS_AGREE));
                Const.friendClientName = screenData.getSendName();
                //截图发送数据
                sendData(ctx,screenData);
            }else {
                //拒绝连接
                ctx.writeAndFlush(new ScreenData(screenData.getReceiveName(),screenData.getSendName(),Const.STATUS_REJECT));
                System.out.println("客户端拒绝对方连接本机");
            }
        }else if(screenData.getStatus() == Const.STATUS_REJECT){
            JOptionPane.showMessageDialog(null,"对方拒绝了您的连接");
        }else if(screenData.getStatus() == Const.STATUS_CLOSE){
            JOptionPane.showMessageDialog(null,"连接断开，请重新连接");
            Const.CONNECT_CLOSE = true;
        }else if(screenData.getStatus() == Const.STATUS_AGREE){
            System.out.println("对方同意连接，将主窗体最小化，并且显示图像窗口");
            //对方同意连接，将主窗体最小话，并且显示图像窗口
            if(!show){
                MainFrame.jFrame.setExtendedState(ICONIFIED); //窗口最小化
                show = true;
            }
            //展示图像
            System.out.println("图片大小= "+screenData.getImageData().length()+",大小="+screenData.getImageData().getBytes().length/1024);
            ViewFrame.INSTANCE().showView(screenData.getImageData());
        }
    }
    /**
     * 使用单独线程发送屏幕截图数据
     * @Author lrh 2020/9/23 16:08
     */
    private void sendData(ChannelHandlerContext ctx,ScreenData screenData){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        Robot finalRobot = robot;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Const.CONNECT_CLOSE){
                    try {
                        System.out.println("发送数据。。。");
                        Rectangle rectangle = new Rectangle(screenSize);
                        BufferedImage screenCapture = finalRobot.createScreenCapture(rectangle);
                        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                        ImageIO.write(screenCapture,"jpg",byteArrayStream);
                        ScreenData sc = new ScreenData(screenData.getReceiveName(),screenData.getSendName(),Const.STATUS_AGREE);
                        String imageData = Base64.getEncoder().encodeToString(byteArrayStream.toByteArray()); ////对图片进行编码
//                        imageData = Util.compress(imageData); //压缩字符串
                        System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
                        sc.setImageData(imageData);
                        ctx.writeAndFlush(sc);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //将标识复位
                Const.CONNECT_CLOSE = false;
            }
        }).start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("客户端异常！！！");
        cause.printStackTrace();
        ctx.close();
    }
}
