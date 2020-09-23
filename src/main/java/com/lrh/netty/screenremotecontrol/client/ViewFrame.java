package com.lrh.netty.screenremotecontrol.client;

import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipOutputStream;

import static java.awt.Frame.ICONIFIED;
import static java.awt.Frame.MAXIMIZED_BOTH;

/**
 * 视图窗口，用来展示图像
 *
 * @Author lrh 2020/9/23 14:35
 */
public class ViewFrame {
    private static ViewFrame INSTANCE;
    private JFrame jFrame = new JFrame("view");
    private JPanel jPanel = new JPanel();
    private JLabel jLabel = new JLabel();

    private ViewFrame() {
        try { // 使用当前系统的界面风格
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            System.out.println("系统界面风格设置失败");
        }
        init();
    }
    
    /**   
     * 初始化
     * @Author lrh 2020/9/23 15:48
     */
    private void init(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setLocation((int)screenSize.getWidth()/2-Const.VIEW_FRAME_WIDTH/2,(int)screenSize.getHeight()/2-Const.VIEW_FRAME_HEIGHT/2);
        jFrame.setSize(Const.VIEW_FRAME_WIDTH,Const.VIEW_FRAME_HEIGHT);
        jFrame.setExtendedState(MAXIMIZED_BOTH);//窗口启动之后最大化
        jLabel.setBackground(Color.BLACK); //设置背景色为黑色
        jLabel.setOpaque(true);
        jPanel.add(jLabel);
        jFrame.add(jPanel);
        jFrame.setVisible(true);
    }
    /**   
     * 获取单例的视图对象
     * @Author lrh 2020/9/23 15:50
     */
    public static ViewFrame INSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new ViewFrame();
        }
        return INSTANCE;
    }
    /**
     * 展示图片
     * @Author lrh 2020/9/23 15:59
     */
    public void showView(String imageData){
        byte[] bytes = Base64.getDecoder().decode(imageData);
        ImageIcon imageIcon = new ImageIcon(bytes);
        jLabel.setIcon(imageIcon);
    }

    /**
     * 事件监听
     * @Author lrh 2020/9/22 16:22
     */
    private void addListener(){
        ComponentListener.closeViewFrameListener(jFrame,MainFrame.friendName.getText().trim());
    }

    public static void main(String[] args) throws AWTException, IOException, InterruptedException {
       INSTANCE();
       Thread.sleep(1000000);
    }



}
