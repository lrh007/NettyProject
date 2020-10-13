package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import com.lrh.netty.screenremotecontrol.client.bean.Const;
import com.lrh.netty.screenremotecontrol.client.bean.ImageData;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    /**
     * 存放上一次的图片数据，用来和这次进行对比
     * @Author lrh 2020/10/9 15:11
     */
    private static Map<Integer, byte[]> beforeImageData = new HashMap<>();
    /**   
     * 保存一个完整的画布
     * @Author lrh 2020/10/13 15:42
     */
    private static BufferedImage globelBufferedImage;
    /**   
     * 保存所有的JLabel组件
     * @Author lrh 2020/10/13 16:44
     */
    public static ConcurrentHashMap<Integer,JLabel> allJLables = new ConcurrentHashMap<>();

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
//        jLabel.setOpaque(true);
        jFrame.setLayout(null);
//        jLabel.setBackground(Color.BLACK); //设置背景色为黑色
//        jPanel.setBackground(Color.BLACK);
//        jLabel.setBounds(0,0,1920,1080);
//        jPanel.add(jLabel);
//        jFrame.add(jPanel);
//        jFrame.add(jLabel);
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
    public synchronized void showView(ImageData imageData){
        //第一种方式，传输整个图片，速度慢
//        byte[] bytes = Util.decodeUnCompress(imageData.getData());
//        ImageIcon imageIcon = new ImageIcon(bytes);
//        jLabel.setIcon(imageIcon);

        //通过jpanel显示图片，特别卡
//        jPanel.display(imageData);

        //第二种方式，分块船速，每次发送过来的数据肯定是发送变化的图片,速度快，但是图片不能合并，导致每次只显示第一张图片
//        beforeImageData.put(imageData.getNumber(),Util.decodeUnCompress(imageData.getData()));
//        System.out.println("beforeImageData条数="+beforeImageData.size());
//        //将map通过key排序
//        Map<Integer, byte[]> sortMap = Util.sortMapByKey(beforeImageData);
//        byte[] bytes = Util.convertMapToBytes(sortMap);
//        ImageIcon imageIcon = new ImageIcon(bytes);
//        jLabel.setIcon(imageIcon);

        //第三种方式，分块传输，判断是否已经创建全局的bufferedImage，速度很慢
//        if(globelBufferedImage == null){
//            globelBufferedImage = new BufferedImage(imageData.getScreenWidth(),imageData.getScreenHeight(),BufferedImage.TYPE_INT_RGB);
//        }
//        Util.mergeImage(globelBufferedImage,imageData);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try {
//            ImageIO.write(globelBufferedImage,"jpg",out);
//            ImageIcon imageIcon = new ImageIcon(out.toByteArray());
//            jLabel.setIcon(imageIcon);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //第四种方式，分块传输，动态创建JLabel，优点：是不用合并图片，缺点：需要创建多个JLabel
//        int x=0,y=0;
//        for(int i=0;i<2;i++){
//            JLabel jLabel = new JLabel();
//            jLabel.setOpaque(true);
//            jLabel.setBackground(Color.BLACK);
//            jLabel.setBounds(x,y,200,20);
//            jFrame.add(jLabel);
//            allJLables.put(i,jLabel);
//            x += 100;
//            y += 100;
//            ComponentListener.updateUI(jFrame);
//        }
        if(allJLables.get(imageData.getNumber()) != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JLabel jLabel = allJLables.get(imageData.getNumber());
                    byte[] bytes = Util.decodeUnCompress(imageData.getData());
                    ImageIcon imageIcon = new ImageIcon(bytes);
                    jLabel.setIcon(imageIcon);
                }
            }).start();

        }else{
            JLabel jLabel = new JLabel();
            jLabel.setOpaque(true);
            jLabel.setBackground(Color.BLACK);
            jLabel.setBounds(imageData.getX(),imageData.getY(),imageData.getWidth(),imageData.getHeight());
            jFrame.add(jLabel);
            allJLables.put(imageData.getNumber(),jLabel);
            ComponentListener.updateUI(jFrame);
        }

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

    public static void main(String[] args) throws Exception {
//        showImage1();
        showImage2();
//        showImage3();
    }


    public static void showImage1()throws Exception  {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = new Robot();
//        Rectangle rectangle = new Rectangle(screenSize);
        Rectangle rectangle = new Rectangle(0,0,192,540);
        ViewFrame instance = INSTANCE();
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        Map<Integer, ImageData> beforeImageData = new HashMap<>(); //存放上一次的图片数据，用来和这次进行对比
        while (true){

//            Thread.sleep(50);

            BufferedImage screenCapture = robot.createScreenCapture(rectangle);


//        screenCapture = screenCapture.getSubimage(0, 0, 300, 300);
            //先把原图片缩小成原来的90%大小，质量为60%
//        BufferedImage bufferedImage1 = Thumbnails.of(screenCapture).scale(0.9f).outputQuality(0.6f).outputFormat("jpg").asBufferedImage();

//        ImageIO.write(screenCapture,"jpg",byteArrayStream);
//        int marginLeft = (int) ((screenSize.getWidth() - screenSize.width*0.9));
            int marginLeft = 0;
//        List<ImageData> imageData1 = Util.splitImage((int) (screenSize.width*0.9), (int) (screenSize.height *0.9),marginLeft, bufferedImage1);
//            Map<Integer, ImageData> imageData1 = Util.splitImageAndNum(screenSize.width, screenSize.height, marginLeft, screenCapture);
            Map<Integer, ImageData> imageData1 = Util.splitImageAndNum(192, 540, marginLeft, screenCapture);
            //如果是第一次，就将当前的数据保存
            if(beforeImageData.size() == 0){
                beforeImageData.putAll(imageData1);
            }
            for (int i = 0; i < imageData1.size(); i++) {
                ImageData data = imageData1.get(i);
                BufferedImage bufferedImage = data.getBufferedImage();
                long s1 = System.currentTimeMillis();
//                boolean b = Util.compareImageData(i, bufferedImage, beforeImageData);
                BufferedImage xorImageData = Util.getXorImageData(i, bufferedImage, beforeImageData);
                System.out.println("图片是否相同= "+xorImageData+",耗时="+(System.currentTimeMillis()-s1));
                if(xorImageData == null){
                    continue;
                }
                BufferedImage restoreXorImageData = Util.restoreXorImageData(bufferedImage, xorImageData);
                ImageIO.write(restoreXorImageData,"jpg",byteArrayStream);
//            ImageIO.write(imageData1.get(0).getBufferedImage(),"jpg",byteArrayStream);
                //        Thumbnails.of(imageData1.get(0).getBufferedImage()).scale(0.9f).outputQuality(0.6f).outputFormat("jpg").toOutputStream(byteArrayStream);
                String imageData = Base64.getEncoder().encodeToString(byteArrayStream.toByteArray()); ////对图片进行编码
                System.out.println("字符个数= "+imageData.length());
                System.out.println("压缩之后字符个数="+Util.encodeAndCompress(byteArrayStream.toByteArray()).length());
                System.out.println("转换之后图片大小="+imageData.getBytes().length/1024);
                System.out.println("压缩之后图片大小="+Util.encodeAndCompress(byteArrayStream.toByteArray()).getBytes().length/1024);
                System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
//            System.out.println(imageData);
                System.out.println("=======================");
                //        ViewFrame instance = INSTANCE();
                //        Graphics g = instance.jPanel.getBufferedImage().getGraphics();

                data.setData(Util.encodeAndCompress(byteArrayStream.toByteArray()));
                instance.jPanel.display(data);
//                instance.showView(data);
                byteArrayStream.reset();
//            g.drawImage(data.getBufferedImage(),data.getX(),data.getY(),data.getWidth(),data.getHeight(),null);
            }
            instance.jPanel.repaint();
        }
//        instance.showView(Util.encodeAndCompress(byteArrayStream.toByteArray()));
//        INSTANCE();
//        Thumbnails.of(screenCapture).scale(1f).outputQuality(0.25f).outputFormat("jpg").toOutputStream(byteArrayStream);
    }

    public static void showImage2() throws Exception{
        ViewFrame instance = INSTANCE();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Rectangle rectangle = new Rectangle(0,0,192,540);
        Rectangle rectangle = new Rectangle(screenSize);
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        Robot robot = new Robot();
        while (true){
//            Thread.sleep(10);
            BufferedImage screenCapture = robot.createScreenCapture(rectangle);
            Map<Integer, ImageData> imageDatas = Util.splitImageAndNum(screenSize.width, screenSize.height, 0, screenCapture);
            for (int i=0;i<imageDatas.size();i++){
                ImageData data = imageDatas.get(i);
                ImageIO.write(data.getBufferedImage(),"jpg",byteArrayStream);
                String imageData = Util.encodeAndCompress(byteArrayStream.toByteArray()); ////对图片进行编码
                System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
                ImageData dataImage = new ImageData(imageData,false,data.getX(),data.getY(),data.getHeight(),data.getWidth(),null,i,screenSize.width,screenSize.height);
                instance.showView(dataImage);
                byteArrayStream.reset();
            }

//            ImageIO.write(screenCapture,"jpg",byteArrayStream);
//            String imageData = Util.encodeAndCompress(byteArrayStream.toByteArray()); ////对图片进行编码
//            System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
//            ImageData dataImage = new ImageData(imageData,false,0,0,540,192,null,0,screenSize.width,screenSize.height);
//            instance.showView(dataImage);
//            byteArrayStream.reset();

        }
    }
    /**   
     * 多线程方式
     * @Author lrh 2020/10/13 17:15
     */
    public static void showImage3() throws Exception{
        ViewFrame instance = INSTANCE();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage screenCapture = robot.createScreenCapture(rectangle);
        Map<Integer, ImageData> imageDatas = Util.splitImageAndNum(screenSize.width, screenSize.height, 0, screenCapture);
        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendMessage(instance,imageDatas.get(0),screenCapture.getWidth(),screenCapture.getHeight());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
//        for(Map.Entry<Integer, ImageData> entry :  imageDatas.entrySet()){
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        sendMessage(instance,entry.getValue(),screenCapture.getWidth(),screenCapture.getHeight());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
    }

    private static void sendMessage(ViewFrame instance,ImageData imageData,int screenWitdh,int screenHeight) throws Exception {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        Rectangle rectangle = new Rectangle(imageData.getX(),imageData.getY(),imageData.getWidth(),imageData.getHeight());
        Robot robot = new Robot();
        while (true){
            Thread.sleep(10);
            BufferedImage screenCapture = robot.createScreenCapture(rectangle);
            ImageIO.write(screenCapture,"jpg",byteArrayStream);
            String data = Util.encodeAndCompress(byteArrayStream.toByteArray()); ////对图片进行编码
            System.out.println("发送之前图片大小="+byteArrayStream.toByteArray().length/1024);
            ImageData dataImage = new ImageData(data,false,imageData.getX(),imageData.getY(),imageData.getHeight(),imageData.getWidth(),null,0,screenWitdh,screenHeight);
            instance.showView(dataImage);
            byteArrayStream.reset();
        }
    }
}
