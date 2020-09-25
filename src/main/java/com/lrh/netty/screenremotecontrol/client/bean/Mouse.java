package com.lrh.netty.screenremotecontrol.client.bean;

import java.io.Serializable;

/**
 * 鼠标实体类
 *
 * @Author lrh 2020/9/24 15:30
 */
public class Mouse implements Serializable {

    /**
     * 鼠标X坐标
     * @Author lrh 2020/9/24 14:15
     */
    private int mouseX;
    /**
     * 鼠标Y坐标
     * @Author lrh 2020/9/24 14:16
     */
    private int mouseY;
    /**
     * 鼠标动作，点击、按下、松开、移动、拖拽、滚轮滑动、进入组件，退出组件
     * @Author lrh 2020/9/24 14:18
     */
    private String mouseAction;
    /**
     * 鼠标左键1，中建2，右键3
     * @Author lrh 2020/9/24 15:20
     */
    private int mouseType;
    /**   
     * 鼠标滚轮转动的距离
     * @Author lrh 2020/9/24 16:03
     */
    private int mouseWhileAmt;

    public Mouse() {
    }

    public Mouse(int mouseX, int mouseY, String mouseAction, int mouseType,int mouseWhileAmt) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseAction = mouseAction;
        this.mouseType = mouseType;
        this.mouseWhileAmt = mouseWhileAmt;
    }

    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public String getMouseAction() {
        return mouseAction;
    }

    public void setMouseAction(String mouseAction) {
        this.mouseAction = mouseAction;
    }

    public int getMouseType() {
        return mouseType;
    }

    public void setMouseType(int mouseType) {
        this.mouseType = mouseType;
    }

    public int getMouseWhileAmt() {
        return mouseWhileAmt;
    }

    public void setMouseWhileAmt(int mouseWhileAmt) {
        this.mouseWhileAmt = mouseWhileAmt;
    }
}
