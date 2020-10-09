package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.client.bean.ImageData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 在jpanel上绘制图片
 *
 * @Author lrh 2020/9/28 12:36
 */
public class ImageJPanel extends JPanel {
    /**   
     * 图像缓冲取
     * @Author lrh 2020/9/28 12:40
     */
    private BufferedImage bufferedImage;
    /**
     * 存放上一次的图片数据，用来和这次进行对比
     * @Author lrh 2020/10/9 15:11
     */
    private static Map<Integer, ImageData> beforeImageData = new HashMap<>();
    /**   
     * 画笔
     * @Author lrh 2020/9/28 14:15
     */
    private Graphics og;


    public ImageJPanel() {
    }

//    @Override
//    public void paint(Graphics g) {
////        super.paint(g);
//        g.drawImage(this.bufferedImage,0,0,this);
//    }
    /**
     * 重新绘制图像
     * @Author lrh 2020/9/28 14:04
     */
    public void display(ImageData data){
        if(og == null){
            this.bufferedImage = (BufferedImage) this.createImage(this.getWidth(), this.getHeight());
            if(this.bufferedImage != null){
                og = this.bufferedImage.getGraphics();
            }
        }
        if(og != null){
            super.paint(og);
            byte[] bytes = Util.decodeUnCompress(data.getData());
            BufferedImage read = null;
            try {
                read = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //将图像异或还原
            if(beforeImageData.get(data.getNumber()) != null){
//                BufferedImage bufferedImage = Util.restoreXorImageData(beforeImageData.get(data.getNumber()).getBufferedImage(), read);
                og.drawImage(read, data.getX(), data.getY(), data.getWidth(), data.getHeight(), this);
            }else{
                data.setBufferedImage(read);
                beforeImageData.put(data.getNumber(),data);
                og.drawImage(read, data.getX(), data.getY(), data.getWidth(), data.getHeight(), this);
            }

        }
        Rectangle rc = new Rectangle(data.getX(),data.getY(),data.getWidth(),data.getHeight());
        this.repaint(rc);
//        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        g.drawImage(this.bufferedImage,0,0,this);
    }

    public BufferedImage getBufferedImage() {
        return (BufferedImage) this.createImage(this.getWidth(), this.getHeight());
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
