package com.lrh.netty.http.proxy3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/9/9 17:51
 */
public class HttpProxyClientTargetHandler extends ChannelInboundHandlerAdapter {
    /**   
     * 代理服务器的channel
     * @Author lrh 2020/9/9 17:52
     */
    private final Channel proxyServerInsideChannel;
    
    public HttpProxyClientTargetHandler(Channel proxyServerInsideChannel) {
        this.proxyServerInsideChannel = proxyServerInsideChannel;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("向代理服务器转发消息： "+((ByteBuf) msg).toString(CharsetUtil.UTF_8));
//        System.out.println("向代理服务器转发消息");
        //向代理服务器转发消息
        if(proxyServerInsideChannel != null){
            ByteBuf b = Unpooled.copiedBuffer("hhhhh",CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,b);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,b.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
            ChannelFuture future = proxyServerInsideChannel.writeAndFlush(msg);
           /* ChannelFuture future = proxyServerInsideChannel.writeAndFlush(msg);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        ctx.channel().read();
                    }else{
                        System.out.println("向代理服务器转发消息失败！");
                        proxyServerInsideChannel.close();
                    }
                }
            });*/
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(proxyServerInsideChannel != null && proxyServerInsideChannel.isActive()){
            proxyServerInsideChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
//        proxyServerInsideChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
