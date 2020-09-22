package com.lrh.netty.screenremotecontrol.client;

import com.lrh.netty.screenremotecontrol.ScreenData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/** 业务处理
 * @Author lrh 2020/9/21 15:15
 */
public class ScreenClientHandler extends SimpleChannelInboundHandler<ScreenData> {
    /**   
     * 客户端自己的名称
     * @Author lrh 2020/9/22 9:55
     */
    public static String myClientName;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ScreenData screenData) throws Exception {
        //获取服务器分配的客户端名称
        if(screenData.getSendName() == null && screenData.getReceiveName() == null){
            myClientName = screenData.getContent();
            System.out.println("服务器分配的名称： "+myClientName);
        }else{
            if(screenData.getSendName().equalsIgnoreCase(myClientName)){
                System.out.println("自己说： "+screenData.getContent());
            }else{
                System.out.println(screenData.getSendName()+"说： "+screenData.getContent());
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("客户端异常！！！");
        cause.printStackTrace();
        ctx.close();
    }
}
