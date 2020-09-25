package com.lrh.netty.screenremotecontrol.client.bean;

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

    public ImageData() {
    }

    public ImageData(String data, boolean isEnd) {
        this.data = data;
        this.isEnd = isEnd;
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
}
