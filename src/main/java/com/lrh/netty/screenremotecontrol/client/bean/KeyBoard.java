package com.lrh.netty.screenremotecontrol.client.bean;

import java.io.Serializable;

/**
 * 键盘实体类
 *
 * @Author lrh 2020/9/24 15:34
 */
public class KeyBoard implements Serializable {
    /**
     * 键盘动作，记录键盘按下的键
     * @Author lrh 2020/9/24 14:23
     */
    private int keyCode;
    /**
     * 键盘事件，按下、释放
     * @Author lrh 2020/9/25 17:41
     */
    private int keyAction;

    public KeyBoard() {
    }

    public KeyBoard(int keyCode, int keyAction) {
        this.keyCode = keyCode;
        this.keyAction = keyAction;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyAction() {
        return keyAction;
    }

    public void setKeyAction(int keyAction) {
        this.keyAction = keyAction;
    }
}
