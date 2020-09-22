package com.lrh.netty.screenremotecontrol;

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

    public ScreenData() {
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
}
