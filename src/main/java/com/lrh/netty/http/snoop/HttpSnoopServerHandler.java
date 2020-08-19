package com.lrh.netty.http.snoop;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;

/**
 * @Author lrh 2020/8/19 11:28
 */
public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest httpRequest;
    //存储响应内容的缓冲区
    private final StringBuilder buf = new StringBuilder();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            HttpRequest request = this.httpRequest = (HttpRequest) msg;
            if(HttpUtil.is100ContinueExpected(request)){
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER));
            }
            buf.setLength(0);
            buf.append("欢迎来到WILD WILD WEB服务器\r\n");
            buf.append("===================================\r\n");
            buf.append("版本： ").append(request.protocolVersion()).append("\r\n");
            buf.append("主机名： ").append(request.headers().get(HttpHeaderNames.HOST,"unknow")).append("\r\n");
            buf.append("请求路径： ").append(request.uri()).append("\r\n");

            HttpHeaders headers = request.headers();
            if(!headers.isEmpty()){
                for (Map.Entry<String,String> h: headers){
                    buf.append("HEADER:").append(h.getKey()).append(" = ").append(h.getValue()).append("\r\n");
                }
                buf.append("\r\n");
            }
        }
        if(msg instanceof HttpContent){
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            if(content.isReadable()){
                buf.append("CONTENT: ").append(content.toString(CharsetUtil.UTF_8)).append("\r\n");
                DecoderResult decoderResult = httpRequest.decoderResult();
                if(decoderResult.isSuccess()){
                    return;
                }
                buf.append("译码器失败: ").append(decoderResult.cause()).append("\r\n");
            }
            if(msg instanceof LastHttpContent){
                LastHttpContent lastHttpContent = (LastHttpContent) msg;
                if(!lastHttpContent.trailingHeaders().isEmpty()){
                    buf.append("\r\n");
                    for(String name:lastHttpContent.trailingHeaders().names()){
                        for (String value:lastHttpContent.trailingHeaders().getAll(name)){
                            buf.append("TRAILING HEADER: ").append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }
                //如果keep-alive关闭，则在内容完全写入后关闭连接
                if(!writeResponse(lastHttpContent,ctx)){
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }

        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        //决定是否关闭连接。
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        //构建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                currentObj.decoderResult().isSuccess()?HttpResponseStatus.OK:HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(),CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
        if(keepAlive){
            //仅为保持活动的连接添加“Content-Length”头。
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
            //添加保持活着的头如下:
            //- http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        }
        //编码cookie
        String cookieStr = httpRequest.headers().get(HttpHeaderNames.COOKIE);
        if(cookieStr != null){
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            if(!cookies.isEmpty()){
                //必要时重置cookie。
                for (Cookie cookie:cookies){
                    response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        }else{
            //浏览器没有发送cookie。添加一些。
            response.headers().set(HttpHeaderNames.SET_COOKIE,ServerCookieEncoder.STRICT.encode("key1","value1"));
            response.headers().set(HttpHeaderNames.SET_COOKIE,ServerCookieEncoder.STRICT.encode("key2","value2"));
        }
        //编写响应。
        ctx.writeAndFlush(response);
        return keepAlive;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
