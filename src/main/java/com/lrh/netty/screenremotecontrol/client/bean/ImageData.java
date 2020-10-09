package com.lrh.netty.screenremotecontrol.client.bean;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * 图片实体类
 *
 * @Author lrh 2020/9/25 13:31
 */
public class ImageData implements Serializable {
    /**   
     * 图片数据
     * @Author lrh 2020/9/25 13:32
     */
    private String data;
    /**   
     * 分段传输时使用，true标识已经传输完成，false 标识传输未完成
     * @Author lrh 2020/9/25 13:33
     */
    private boolean isEnd;
    /**
     * 图片X坐标
     * @Author lrh 2020/9/28 12:04
     */
    private int x;
    /**
     * 图片y坐标
     * @Author lrh 2020/9/28 12:04
     */
    private int y;
    /**
     * 图片高度
     * @Author lrh 2020/9/28 12:04
     */
    private int height;
    /**
     * 图片宽度
     * @Author lrh 2020/9/28 12:04
     */
    private int width;
    /**   
     * 图片分割缓冲区，仅用于分割图片时使用
     * @Author lrh 2020/9/28 12:12
     */
    private BufferedImage bufferedImage;
    /**
     * 图片编号
     * @Author lrh 2020/10/9 15:36
     */
    private int number;

    public ImageData() {
    }

    public ImageData(String data, boolean isEnd, int x, int y, int height, int width, BufferedImage bufferedImage,int number) {
        this.data = data;
        this.isEnd = isEnd;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.bufferedImage = bufferedImage;
        this.number = number;
    }

    public ImageData(String data, boolean isEnd, int x, int y, int height, int width,int number) {
        this(data,isEnd,x,y,height,width,null,number);
    }

    public ImageData(String data, boolean isEnd) {
        this(data,isEnd,0,0,0,0,0);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
