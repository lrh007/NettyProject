package com.lrh.netty.http.portunification;

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

import java.util.Map;
import java.util.Set;

/**
 * http业务处理器
 *
 * @Author lrh 2020/9/3 15:49
 */
public class HttpSnoopHandler extends SimpleChannelInboundHandler<Object> {
    private StringBuffer buffer = new StringBuffer();
    private HttpRequest httpRequest;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            HttpRequest request = (HttpRequest) msg;
            this.httpRequest = request;
            if(HttpUtil.is100ContinueExpected(request)){
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER));
            }
            buffer.setLength(0);
            buffer.append("============欢迎光临===========\r\n");
            buffer.append("协议版本：").append(request.protocolVersion()).append("\r\n");
            buffer.append("主机名：").append(request.headers().get(HttpHeaderNames.HOST,"unknow")).append("\r\n");
            buffer.append("请求路径：").append(request.uri()).append("\r\n");

            //获取所有请求头信息
            HttpHeaders headers = request.headers();
            for(Map.Entry<String,String> map: headers){
                buffer.append("HEADER: ").append(map.getKey()).append(" = ").append(map.getValue()).append("\r\n");
            }
        }
        if(msg instanceof HttpContent){
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            if(content.isReadable()){
                buffer.append("CONTENT: ").append(content.toString(CharsetUtil.UTF_8)).append("\r\n");
                DecoderResult decoderResult = httpRequest.decoderResult();
                if(decoderResult.isSuccess()){
                    return;
                }
                buffer.append("解码失败：").append(decoderResult.cause()).append("\r\n");
            }
            //读取最后的请求
            if(msg instanceof LastHttpContent){
                LastHttpContent lastHttpContent = (LastHttpContent) msg;
                //查询最后的所有头信息
                if(!lastHttpContent.trailingHeaders().isEmpty()){
                    buffer.append("\r\n");
                    Set<String> names = lastHttpContent.trailingHeaders().names();
                    for(String name:names){
                        //查询全部的value
                        for (String key:lastHttpContent.trailingHeaders().getAll(name)){
                            buffer.append("TRAILING HEADER: ").append(name).append(" = ").append(key).append("\r\n");
                        }
                    }
                    buffer.append("\r\n");
                }
                //如果keep-alive关闭，则在内容完全写入后关闭连接
                if(!writeAndFlush(lastHttpContent,ctx)){
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }

            }
        }

    }

    private boolean writeAndFlush(HttpObject currentObj,ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                currentObj.decoderResult().isSuccess()?HttpResponseStatus.OK:HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(buffer.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);
        if(keepAlive){
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        }
        //设置返回的cookie
        String cookieStr = httpRequest.headers().get(HttpHeaderNames.COOKIE);
        if(cookieStr != null){
            //解码cookie
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for(Cookie cookie : cookies){
                response.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }else{
            ////浏览器没有发送cookie。添加一些。
            response.headers().set(HttpHeaderNames.COOKIE,ServerCookieEncoder.STRICT.encode("name1","value1"));
            response.headers().set(HttpHeaderNames.COOKIE,ServerCookieEncoder.STRICT.encode("name2","value2"));
        }
        ctx.writeAndFlush(response);
        return keepAlive;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
