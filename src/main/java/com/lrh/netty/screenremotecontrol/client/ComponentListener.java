package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import io.netty.channel.ChannelFuture;

import javax.swing.*;
import java.awt.event.*;

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
    public static void closeMainFrameListener(JFrame jFrame,JTextField friendName) {
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(ScreenClient.serverChannel != null && ScreenClient.serverChannel.isActive()){
                    if(!"".equals(friendName.getText().trim())){
                        ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName, friendName.getText().trim(), Const.STATUS_CLOSE));
                    }else{
                        ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName, Const.friendClientName, Const.STATUS_CLOSE));
                    }
                }
                System.out.println("关闭MainFrame,myClientName="+Const.myClientName+",friend="+Const.friendClientName+",client="+friendName.getText().trim());
                System.exit(0);
            }
        });
    }
    /**
     * 关闭ViewFrame窗口时调用
     * @Author lrh 2020/9/23 18:08
     */
    public static void closeViewFrameListener(JFrame jFrame,JTextField friendName) {
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(ScreenClient.serverChannel != null && ScreenClient.serverChannel.isActive()){
                    if(!"".equals(friendName.getText().trim())){
                        ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName, friendName.getText().trim(), Const.STATUS_CLOSE));
                    }else{
                        ScreenClient.serverChannel.writeAndFlush(new ScreenData(Const.myClientName, Const.friendClientName, Const.STATUS_CLOSE));
                    }
                }
                System.exit(0);
            }
        });
    }
    /**   
     * viewFrame 鼠标事件监听
     * @Author lrh 2020/9/24 9:33
     */
    public static void viewFrameMouseListener(JLabel jLabel, JTextField friendName) {
        jLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("鼠标拖拽。。。");
                setMouseInfo(e.getX(),e.getY(),Const.mouseDragged,e.getButton(),0);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println("鼠标移动。。。");
                setMouseInfo(e.getX(),e.getY(),Const.mouseMoved,e.getButton(),0);
            }
        });
        jLabel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                System.out.println("鼠标滚轮滑动。。。"+e.getScrollAmount()+","+e.getScrollType()+","+e.getWheelRotation()+","+e.getUnitsToScroll());
                setMouseInfo(e.getX(),e.getY(),Const.mouseWheelMoved,e.getButton(),e.getUnitsToScroll());
            }
        });
        jLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("鼠标点击:"+e.getButton());
                setMouseInfo(e.getX(),e.getY(),Const.mouseClicked,e.getButton(),0);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("鼠标按下");
                setMouseInfo(e.getX(),e.getY(),Const.mousePressed,e.getButton(),0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("鼠标松开");
                setMouseInfo(e.getX(),e.getY(),Const.mouseReleased,e.getButton(),0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("鼠标进入组件");
                Const.MOUSE_ON = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("鼠标退出组件");
                Const.MOUSE_ON = false;
            }

        });
    }
    /**
     * 设置鼠标信息
     * @Author lrh 2020/9/24 15:08
     */
    public static void setMouseInfo(int x,int y,String action,int mouseType,int mouseWhileAmt){
//        Const.MOUSE_X = x;
//        Const.MOUSE_Y = y;
//        Const.mouseAction = action;
//        Const.mouseType = mouseType;
//        Const.MOUSE_WHILEAMT = mouseWhileAmt;
        //向其他客户端发送鼠标消息
        if(ScreenClient.serverChannel != null && ScreenClient.serverChannel.isActive()){
            ScreenData sc = new ScreenData(Const.mouseSendClientName,Const.mouseReceiveClientName,Const.STATUS_AGREE,null);
            Mouse mouse = new Mouse(x,y,action,mouseType,mouseWhileAmt);
            sc.setMouse(mouse);
            ScreenClient.serverChannel.writeAndFlush(sc);
        }
    }

    /**   
     * ViewFrame 键盘事件监听
     * @Author lrh 2020/9/24 14:49
     */
    public static void viewFrameKeyBoardListener(JLabel jLabel, JTextField friendName) {
        jLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println("键盘按键类型"+e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("键盘按键按下");
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("键盘按键释放");
            }
        });

    }
}
