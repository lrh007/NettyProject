package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.client.bean.Const;
import com.lrh.netty.screenremotecontrol.client.bean.ImageData;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static java.awt.Frame.MAXIMIZED_BOTH;

/**
 * 视图窗口，用来展示图像
 *
 * @Author lrh 2020/9/23 14:35
 */
public class ViewFrame {
    private static ViewFrame INSTANCE;
    private JFrame jFrame = new JFrame("view");
    private ImageJPanel jPanel = new ImageJPanel();
    private JLabel jLabel = new JLabel();

    private ViewFrame() {
        try { // 使用当前系统的界面风格
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            System.out.println("系统界面风格设置失败");
        }
        init();
        addListener();
    }
    
    /**   
     * 初始化
     * @Author lrh 2020/9/23 15:48
     */
    private void init(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setLocation((int)screenSize.getWidth()/2- Const.VIEW_FRAME_WIDTH/2,(int)screenSize.getHeight()/2-Const.VIEW_FRAME_HEIGHT/2);
        jFrame.setSize(Const.VIEW_FRAME_WIDTH,Const.VIEW_FRAME_HEIGHT);
        jFrame.setExtendedState(MAXIMIZED_BOTH);//窗口启动之后最大化
        jLabel.setOpaque(true);
        jLabel.setBackground(Color.BLACK); //设置背景色为黑色
//        jPanel.setBackground(Color.BLACK);
        jLabel.setSize(500,500);
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
     * ViewFrame窗体销毁
     * @Author lrh 2020/9/25 15:57
     */
    public static void dispose(){
        INSTANCE().jFrame.dispose();
        INSTANCE = null;
    }
    /**
     * 展示图片
     * @Author lrh 2020/9/23 15:59
     */
    public void showView(ImageData imageData){
//        byte[] bytes = Util.decodeUnCompress(imageData);
//        ImageIcon imageIcon = new ImageIcon(bytes);
//        jLabel.setIcon(imageIcon);
        jPanel.display(imageData);
    }

    /**
     * 事件监听
     * @Author lrh 2020/9/22 16:22
     */
    private void addListener(){
        ComponentListener.closeViewFrameListener(jFrame,MainFrame.friendName);
        ComponentListener.viewFrameMouseListener(jLabel,MainFrame.friendName);
        ComponentListener.viewFrameKeyBoardListener(MainFrame.friendName);

    }

    public static void main(String[] args) throws AWTException, IOException, InterruptedException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = new Robot();
        Rectangle rectangle = new Rectangle(screenSize);
        ViewFrame instance = INSTANCE();
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        while (true){

            Thread.sleep(50);

        BufferedImage screenCapture = robot.createScreenCapture(rectangle);


//        screenCapture = screenCapture.getSubimage(0, 0, 300, 300);
        //先把原图片缩小成原来的90%大小，质量为60%
        BufferedImage bufferedImage1 = Thumbnails.of(screenCapture).scale(0.9f).outputQuality(0.6f).outputFormat("jpg").asBufferedImage();

//        ImageIO.write(screenCapture,"jpg",byteArrayStream);
//        int marginLeft = (int) ((screenSize.getWidth() - screenSize.width*0.9));
        int marginLeft = 0;
        List<ImageData> imageData1 = Util.splitImage((int) (screenSize.width*0.9), (int) (screenSize.height *0.9),marginLeft, bufferedImage1);
        for (int i = 0; i < imageData1.size(); i++) {
            ImageIO.write(imageData1.get(i).getBufferedImage(),"jpg",byteArrayStream);
//            ImageIO.write(imageData1.get(0).getBufferedImage(),"jpg",byteArrayStream);
    //        Thumbnails.of(imageData1.get(0).getBufferedImage()).scale(0.9f).outputQuality(0.6f).outputFormat("jpg").toOutputStream(byteArrayStream);
            String imageData = Base64.getEncoder().encodeToString(byteArrayStream.toByteArray()); ////对图片进行编码
            System.out.println("字符个数= "+imageData.length());
            System.out.println("压缩之后字符个数="+Util.encodeAndCompress(byteArrayStream.toByteArray()).length());
            System.out.println("转换之后图片大小="+imageData.getBytes().length/1024);
            System.out.println("压缩之后图片大小="+Util.encodeAndCompress(byteArrayStream.toByteArray()).getBytes().length/1024);
            System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
            System.out.println(imageData);
    //        ViewFrame instance = INSTANCE();
    //        Graphics g = instance.jPanel.getBufferedImage().getGraphics();

            ImageData data = imageData1.get(i);
            data.setData(Util.encodeAndCompress(byteArrayStream.toByteArray()));
            instance.jPanel.display(data);
            byteArrayStream.reset();
//            g.drawImage(data.getBufferedImage(),data.getX(),data.getY(),data.getWidth(),data.getHeight(),null);
          }
        }
//        instance.showView(Util.encodeAndCompress(byteArrayStream.toByteArray()));
//        INSTANCE();

//        Thumbnails.of(screenCapture).scale(1f).outputQuality(0.25f).outputFormat("jpg").toOutputStream(byteArrayStream);
    }



}
