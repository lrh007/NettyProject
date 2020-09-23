package com.lrh.netty.screenremotecontrol;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * 数据实体类
 *
 * @Author lrh 2020/9/21 15:57
 */
public class ScreenData implements Serializable {
    /**
     * 数据发送方客户端名称
     * @Author lrh 2020/9/21 16:41
     */
    private String sendName;
    /**   
     * 数据接收方客户端名称
     * @Author lrh 2020/9/21 16:01
     */
    private String receiveName;
    /**   
     * 要发送的数据内容
     * @Author lrh 2020/9/21 16:01
     */
    private String content;
    /**
     * 标识位，用来进行各种判断
     * @Author lrh 2020/9/23 10:06
     */
    private int status;
    /**   
     * 图片数据
     * @Author lrh 2020/9/23 16:05
     */
    private String imageData;

    public ScreenData() {
    }

    public ScreenData(String sendName, String receiveName, String content, int status) {
        this.sendName = sendName;
        this.receiveName = receiveName;
        this.content = content;
        this.status = status;
    }
    public ScreenData(String sendName, String receiveName, int status) {
        this(sendName,receiveName,null,status);
    }

    public ScreenData(String sendName, String receiveName) {
        this(sendName,receiveName,-1);
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
}
