package com.lrh.netty.screenremotecontrol;

import com.lrh.netty.screenremotecontrol.client.KeyBoard;
import com.lrh.netty.screenremotecontrol.client.Mouse;

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
    /**
     * 鼠标事件
     * @Author lrh 2020/9/24 15:35
     */
    private Mouse mouse;
    /**
     * 键盘事件
     * @Author lrh 2020/9/24 15:36
     */
    private KeyBoard keyBoard;


    public ScreenData() {
    }

    public ScreenData(String sendName, String receiveName, String content, int status, String imageData, Mouse mouse, KeyBoard keyBoard) {
        this.sendName = sendName;
        this.receiveName = receiveName;
        this.content = content;
        this.status = status;
        this.imageData = imageData;
        this.mouse = mouse;
        this.keyBoard = keyBoard;
    }

    public ScreenData(String sendName, String receiveName, String content, int status, String imageData) {
        this(sendName,receiveName,content,status,imageData,null,null);
    }

    public ScreenData(String sendName, String receiveName, String content, int status) {
        this(sendName,receiveName,content,status,null);
    }

    public ScreenData(String sendName, String receiveName, int status, String imageData) {
       this(sendName,receiveName,null,status,imageData);
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

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public KeyBoard getKeyBoard() {
        return keyBoard;
    }

    public void setKeyBoard(KeyBoard keyBoard) {
        this.keyBoard = keyBoard;
    }
}
