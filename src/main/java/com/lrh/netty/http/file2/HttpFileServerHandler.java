package com.lrh.netty.http.file2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @Author lrh 2020/12/23 14:26
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String defaultUrl;

    public HttpFileServerHandler(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //判断是否解码成功
        if(!request.decoderResult().isSuccess()){
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        //判断是否是GET方式请求
        if(request.method() != HttpMethod.GET){
            sendError(ctx,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        //判断路径是否合法
        if(path == null){
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }
    }
    /**   
     * 转换路径
     * @Author lrh 2020/12/23 15:29
     */
    private String sanitizeUri(String uri) {
        return null;
    }

    /**
     * 返回错误值
     * @Author lrh 2020/12/23 15:18
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status,
                Unpooled.copiedBuffer("Failure: "+status.toString()+"\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
