package com.lrh.netty.binary.factorial;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author lrh 2020/8/26 17:59
 */
public class FactorialClientHandler extends SimpleChannelInboundHandler<BigInteger> {
    private ChannelHandlerContext ctx;
    private int receveMessage;
    private int next = 1;
    final BlockingQueue<BigInteger> answer = new LinkedBlockingQueue<>();

    /**   
     * 获取数据
     * @Author lrh 2020/8/26 18:42
     */
    public BigInteger getFactorial(){
        boolean interupted = false;
        try{
            while(true){
                try {
                    return answer.take();
                } catch (InterruptedException e) {
                    interupted = true;
                }
            }
        }finally {
            if(interupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        sendNumbers();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BigInteger msg) throws Exception {
        receveMessage ++;
        if(receveMessage == FactorialClient.COUNT){
            //在关闭连接后提供answer。
            ctx.channel().close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    //向队列中添加数据
                    answer.offer(msg);
                }
            });
        }
    }

    private void sendNumbers(){
        //不发送超过4096个数字
        ChannelFuture future = null;
        for (int i = 0; i < 4096 && next <= FactorialClient.COUNT; i++) {
            future = ctx.writeAndFlush(next);
            next ++;
        }
        if(next <= FactorialClient.COUNT){
            future.addListener(numberSender);
        }
    }

    private final ChannelFutureListener numberSender = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if(channelFuture.isSuccess()){
                sendNumbers();
            }else{
                channelFuture.cause().printStackTrace();
                channelFuture.channel().close();
            }
        }
    };



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
