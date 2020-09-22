package com.lrh.netty.screenremotecontrol.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    public static void connectServerListener(JButton jButton,JTextField friendName){
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clientName = friendName.getText().trim();
                if(!Const.CONNNECT_SUCCESS){
                    JOptionPane.showMessageDialog(null,"请等待连接服务器");
                }else if("".equals(clientName)){
                    JOptionPane.showMessageDialog(null,"请输入伙伴识别码");
                }else if(clientName.length() != 9){
                    JOptionPane.showMessageDialog(null,"伙伴识别码错误");
                } else{
                    System.out.println("aaa");

                }

            }
        });
    }
}
