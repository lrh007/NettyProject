package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 组件的事件监听
 *
 * @Author lrh 2020/9/22 16:21
 */
public class ComponentListener {
    /**
     * 连接服务器事件监听（点击远程协助按钮）
     * @Author lrh 2020/9/22 16:34
     */
    public static void connectServerListener(JButton jButton,JTextField friendName,JLabel myClientName){
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clientName = friendName.getText().trim();
                if(!Const.CONNECT_SUCCESS){
                    JOptionPane.showMessageDialog(null,"请等待连接服务器");
                }else if("".equals(clientName)){
                    JOptionPane.showMessageDialog(null,"请输入伙伴识别码");
                }else if(clientName.length() != 9){
                    JOptionPane.showMessageDialog(null,"伙伴识别码错误");
                } else if(clientName.equals(Const.myClientName)){
                    JOptionPane.showMessageDialog(null,"伙伴识别码不能和自己相同");
                }else if(!ScreenClient.serverChannel.isActive() && !Const.CONNECT_RETRY){
                    JOptionPane.showMessageDialog(null,"服务器连接异常，请重启软件");
                    Const.CONNECT_SUCCESS = false;
                    Const.CONNECT_RETRY = true;
                    myClientName.setText("--- --- ---");
                    MainFrame.setErrorMsg(" 服务器连接异常，请重启软件");
                }else{
                    //发送消息给其他客户端，请求接受连接
                    ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName,clientName,Const.STATUS_RECEIVE));
                    System.out.println("发送消息给其他客户端，请求接受连接，channel="+ScreenClient.serverChannel.isActive());
                }
            }
        });
    }
    /**
     * 关闭MainFrame窗口时调用
     * @Author lrh 2020/9/23 18:08
     */
    public static void closeMainFrameListener(JFrame jFrame,String clientName) {
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName,clientName,Const.STATUS_CLOSE));
                System.exit(0);
            }
        });
    }
    /**
     * 关闭ViewFrame窗口时调用
     * @Author lrh 2020/9/23 18:08
     */
    public static void closeViewFrameListener(JFrame jFrame,String clientName) {
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName,clientName,Const.STATUS_CLOSE));
                System.exit(0);
            }
        });
    }
}
