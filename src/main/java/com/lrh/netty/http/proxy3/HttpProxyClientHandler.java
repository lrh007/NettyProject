package com.lrh.netty.http.proxy3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/9/9 17:07
 */
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 目标服务器的channel
     *
     * @Author lrh 2020/9/9 17:34
     */
    private Channel targetChannel;
    /**
     * 目标服务器ip
     * @Author lrh 2020/9/11 9:25
     */
    private final String targetHost;
    /** 目标服务器端口
     * @Author lrh 2020/9/11 9:25
     */
    private final int targetPort;
    private Channel proxyServerInsideChannel;

    public HttpProxyClientHandler(){
        this.targetHost =  HttpProxyClient.TARGET_HOST;
        this.targetPort = HttpProxyClient.TARGET_PORT;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //代理服务器的内部处理器的channel
        final Channel proxyServerInsideChannel = ctx.channel();
        //创建新的连接，连接到目标服务器,使用代理客户端的线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
//                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new HttpResponseEncoder());
                        pipeline.addLast(new HttpObjectAggregator(6553600));
                        pipeline.addLast(new HttpProxyClientTargetHandler(proxyServerInsideChannel));
                    }
                }).option(ChannelOption.AUTO_READ,false);
        ChannelFuture future = b.connect(targetHost, targetPort);
        this.targetChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                targetChannel = channelFuture.channel();
                System.out.println("ctx2isActive="+ctx.channel().isActive()+",isOpen="+ctx.channel().isOpen()+",isWritable="+ctx.channel().isWritable()+",class="+targetChannel.hashCode());
                if(channelFuture.isSuccess()){
                    System.out.println("目标服务器连接成功，通知代理服务器读取数据");
                    ctx.channel().read();
                }else{
                    System.out.println("目标服务器连接失败,关闭代理客户端，地址： "+targetHost+":"+targetPort);
                    proxyServerInsideChannel.close();
                }
            }
        });
        System.out.println("ctxisActive="+ctx.channel().isActive()+",isOpen="+ctx.channel().isOpen()+",isWritable="+ctx.channel().isWritable()+",class="+targetChannel.hashCode());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //向目标服务器转发消息
//        ByteBuf buf = (ByteBuf) msg;
//        System.out.println("向目标服务器转发消息： "+buf.toString(CharsetUtil.UTF_8));
        System.out.println("向目标服务器转发消息");
        if (targetChannel != null && targetChannel.isActive()) {
            System.out.println("isActive="+targetChannel.isActive()+",isOpen="+targetChannel.isOpen()+",isWritable="+targetChannel.isWritable());
            ChannelFuture future = targetChannel.writeAndFlush(msg);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        ctx.channel().read();
                    }else{
                        System.out.println("向目标服务器转发消息失败！");
                        targetChannel.close();
                    }
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(targetChannel != null && targetChannel.isActive()){
            targetChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
