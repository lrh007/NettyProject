package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import com.lrh.netty.screenremotecontrol.client.bean.Const;
import com.lrh.netty.screenremotecontrol.client.bean.ImageData;
import com.lrh.netty.screenremotecontrol.client.bean.KeyBoard;
import com.lrh.netty.screenremotecontrol.client.bean.Mouse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    /**
     * 存放上一次的图片数据，用来和这次进行对比
     * @Author lrh 2020/10/9 15:11
     */
    private static ConcurrentHashMap<Integer, ImageData> beforeImageData = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ScreenData screenData) throws Exception {
        //获取服务器分配的客户端名称
        if(screenData.getSendName() == null && screenData.getReceiveName() == null){
            Const.myClientName = screenData.getContent();
            System.out.println("服务器分配的名称： "+Const.myClientName);
        }else{
            if(screenData.getSendName().equalsIgnoreCase(Const.myClientName) && Const.STATUS_NOT_FOUND == screenData.getStatus()){
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
            Const.CONNECT_CLOSE = true;
            ViewFrame.dispose(); //销毁窗体，释放资源
            JOptionPane.showMessageDialog(null,"连接断开，请重新连接");
        }else if(screenData.getStatus() == Const.STATUS_AGREE){
            System.out.println("对方同意连接，将主窗体最小化，并且显示图像窗口");
            Const.mouseSendClientName = screenData.getReceiveName();
            Const.mouseReceiveClientName = screenData.getSendName();
            //对方同意连接，将主窗体最小话，并且显示图像窗口
            if(!show){
                MainFrame.jFrame.setExtendedState(ICONIFIED); //窗口最小化
                show = true;
            }
            //展示图像
            //因为鼠标事件也需要发送消息，所以这里要判断一下
            if(screenData.getImageData() != null){
                System.out.println("图片大小= "+screenData.getImageData().getData().length()+",大小="+screenData.getImageData().getData().getBytes().length/1024);
                ViewFrame.INSTANCE().showView(screenData.getImageData());
            }
//            handlerMouseEvent(screenData.getMouse());
//            handlerKeyBoardEvent(screenData.getKeyBoard());
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
        Rectangle rectangle = new Rectangle(screenSize);
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        System.out.println("发送数据,连接关闭="+Const.CONNECT_CLOSE);
        ViewFrame.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                boolean first = false;
                while(!Const.CONNECT_CLOSE){
                    try {
                        System.out.println("发送数据,CONNECT_CLOSE="+Const.CONNECT_CLOSE);
                        BufferedImage screenCapture = finalRobot.createScreenCapture(rectangle);
                        //先把原图片缩小成原来的90%大小，质量为60%
                        screenCapture = Thumbnails.of(screenCapture).scale(Const.scale).outputQuality(1f).outputFormat("jpg").asBufferedImage();
                        //分割图片
//                        List<ImageData> imageDatas = Util.splitImage((int) (screenSize.width*0.9), (int) (screenSize.height *0.9),0, bufferedImage);
                        Map<Integer, ImageData> imageDatas = Util.splitImageAndNum((int)(screenSize.width*Const.scale), (int)(screenSize.height*Const.scale), 0, screenCapture);
                        //如果是第一次，就将当前的数据保存
                        if(beforeImageData.size() == 0){
                            beforeImageData.putAll(imageDatas);
                            //第一次将图片全部发送给对方，保证能正常显示
                            for (int i=0;i<imageDatas.size();i++){
                                ImageData data = imageDatas.get(i);
                                ImageIO.write(data.getBufferedImage(),"jpg",byteArrayStream);
                                String imageData = Util.encodeAndCompress(byteArrayStream.toByteArray()); ////对图片进行编码
                                System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
                                ImageData dataImage = new ImageData(imageData,false,data.getX(),data.getY(),data.getHeight(),data.getWidth(),null,data.getNumber(),screenSize.width,screenSize.height);
                                ScreenData sc = new ScreenData(screenData.getReceiveName(),screenData.getSendName(),Const.STATUS_AGREE,dataImage);
                                ctx.writeAndFlush(sc);
                                byteArrayStream.reset();
                            }
                        }
                        for (int i=0;i<imageDatas.size();i++){
                            ImageData data = imageDatas.get(i);
                            //用来保存之前的数据进行异或操作
                            BufferedImage beforeBufferedImage = beforeImageData.get(data.getNumber()).getBufferedImage();
                            boolean b = Util.compareImageData(data.getNumber(), data.getBufferedImage(), beforeBufferedImage);
                            if(!b){
                                //将原来的图片替换保存
                                beforeImageData.get(data.getNumber()).setBufferedImage(data.getBufferedImage());
                                byte[] bytes = Util.encodeImage(data.getBufferedImage());
                                String imageData = Util.encodeAndCompress(bytes); ////对图片进行编码
                                System.out.println("发送之前图片大小="+bytes.length/1024);
                                ImageData dataImage = new ImageData(imageData,false,data.getX(),data.getY(),data.getHeight(),data.getWidth(),null,data.getNumber(),screenSize.width,screenSize.height);
                                ScreenData sc = new ScreenData(screenData.getReceiveName(),screenData.getSendName(),Const.STATUS_AGREE,dataImage);
                                ctx.writeAndFlush(sc);
                                data = null;
                            }
                        }
                        Thread.sleep(Const.SEND_DATA_INTERVAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //将标识复位
                Const.CONNECT_CLOSE = false;
            }
        });
    }

    /**
     * 处理鼠标事件
     * @Author lrh 2020/9/24 15:42
     */
    private void handlerMouseEvent(Mouse mouse){
        if(mouse != null){
            try {
                Robot robot = new Robot();
                int buttons = mouseBtnTypeConvert(mouse.getMouseType()); //鼠标按键类型转换
                if(Const.mouseClicked.equals(mouse.getMouseAction())){
                    //鼠标点击事件,包含按下、松开两个动作
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    robot.mousePress(buttons);
                    robot.mouseRelease(buttons);
                    System.out.println("处理鼠标点击事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else if(Const.mousePressed.equals(mouse.getMouseAction())){
                    //鼠标按下事件
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    robot.mousePress(buttons);
                    System.out.println("处理鼠标按下事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else if(Const.mouseReleased.equals(mouse.getMouseAction())){
                    //鼠标释放事件
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    robot.mouseRelease(buttons);
                    System.out.println("处理鼠标释放事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else if(Const.mouseWheelMoved.equals(mouse.getMouseAction())){
                    //鼠标滚轮滑动事件
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    robot.mouseWheel(mouse.getMouseWhileAmt());
                    System.out.println("处理鼠标滚轮滑动事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else if(Const.mouseMoved.equals(mouse.getMouseAction())){
                    //鼠标移动事件
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    System.out.println("处理鼠标移动事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else if(Const.mouseDragged.equals(mouse.getMouseAction())){
                    //鼠标拖拽事件
                    robot.mouseMove(mouse.getMouseX(),mouse.getMouseY());
                    System.out.println("处理鼠标拖拽事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }else{
                    System.out.println("未知鼠标事件：[x="+mouse.getMouseX()+",y="+mouse.getMouseY()+",type="+mouse.getMouseType()+",action="+mouse.getMouseAction()+"]");
                }
            } catch (AWTException e) {
                System.out.println("处理鼠标事件异常："+e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**   
     * 鼠标按键类型转换
     * @Author lrh 2020/9/24 17:54
     */
    private int mouseBtnTypeConvert(int mouseType){
        //鼠标左键
        if(mouseType == 1){
            return InputEvent.BUTTON1_MASK;
        }
        //鼠标中键
        if(mouseType == 2){
            return InputEvent.BUTTON2_MASK;
        }
        //鼠标右键
        if(mouseType == 3){
            return InputEvent.BUTTON3_MASK;
        }
        return -1;
    }

    /**   
     * 处理键盘事件
     * @Author lrh 2020/9/25 17:35
     */
    private void handlerKeyBoardEvent(KeyBoard keyBoard){
        if(keyBoard != null){
            try {
                Robot robot = new Robot();
                //键盘按下事件
                if(keyBoard.getKeyAction() == KeyEvent.KEY_PRESSED){
                    robot.keyPress(keyBoard.getKeyCode());
                }
                //键盘松开事件
                if(keyBoard.getKeyAction() == KeyEvent.KEY_RELEASED){
                    robot.keyRelease(keyBoard.getKeyCode());
                }

            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("客户端异常！！！");
        JOptionPane.showMessageDialog(null,"客户端异常，请重启软件");
        cause.printStackTrace();
        ctx.close();
    }
}
