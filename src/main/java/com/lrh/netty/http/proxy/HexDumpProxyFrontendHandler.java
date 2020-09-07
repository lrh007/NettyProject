package com.lrh.netty.http.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**前端处理器
 * @Author lrh 2020/9/2 9:25
 */
public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private final String remoteHost;
    private final int remotePort;
    //因为我们在构建引导程序时使用inboundChannel.eventLoop()，所以不需要是volatile As
    //outboundChannel将使用与inboundChannel相同的EventLoop(因此线程)。
    //将请求写入到目标服务器
    private Channel outboundChannel;

    public HexDumpProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel();
        //代理服务器启动后启动连接尝试被代理服务器。
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()) //使用原来的线程池
                .channel(ctx.channel().getClass())
                .handler(new HexDumpProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ,false);
        ChannelFuture future = b.connect(remoteHost, remotePort);
        outboundChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                    //连接完成，开始读取第一个数据
                    inboundChannel.read();
                }else{
                    //如果连接尝试失败，请关闭连接。
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(outboundChannel.isActive()){
            ChannelFuture future = outboundChannel.writeAndFlush(msg);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        //是否能够冲洗出数据，开始读取下一个区块
                        ctx.channel().read();
                    }else{
                        channelFuture.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(outboundChannel != null){
            closeOnFlush(outboundChannel);
        }
    }
    /**
     * 在所有排队写请求刷新后关闭指定的通道。
     * @Author lrh 2020/9/2 9:45
     */
    public static void closeOnFlush(Channel ch){
        if(ch.isActive()){
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }
}
