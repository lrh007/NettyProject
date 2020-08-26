package com.lrh.netty.http.file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @Author lrh 2020/8/19 16:06
 */
public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    //时间格式
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    //时区
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    //缓存时间
    public static final int HTTP_CACHE_SECONDS = 60;
    //匹配url
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-\\._]?[^<>&\\\"]*");
    private FullHttpRequest fullHttpRequest;
    //默认根目录文件夹
    private static final String DEFAULT_ROOT_PATH = System.getProperty("user.dir");
//    private static final String DEFAULT_ROOT_PATH = "E:\\JavaProject";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        this.fullHttpRequest = request;
        if (!request.decoderResult().isSuccess()) {
            this.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (!HttpMethod.GET.equals(request.method())) {
            this.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        final String uri = request.uri();
        final String path = this.sanitizeUri(uri);
        if (path == null) {
            this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                this.sendListing(ctx, file, uri);
            } else {
                this.sendRedirect(ctx, uri + '/');
            }
            return;
        }
        if (!file.isFile()) {
            this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }
        //验证缓存
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
            // 只与第二个比较，因为我们发送给客户机的datetime格式，没有毫秒
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                this.sendNotModified(ctx);
                return;
            }
        }
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file,"r");
        } catch (FileNotFoundException e) {
            this.sendError(ctx,NOT_FOUND);
            return;
        }
        long fileLength = file.length();
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1,OK);
        HttpUtil.setContentLength(response,fileLength); //设置文件大小
        this.setContentTypeHeader(response, file);
        this.setDateAndCacheHeaders(response, file);
        if(!keepAlive){
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);
        }else if(request.protocolVersion().equals(HTTP_1_0)){
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        }
        //写出初始行和标题
        ctx.writeAndFlush(response);
        //写出内容
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if(ctx.pipeline().get(SslHandler.class) == null){
            sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(),0,fileLength),ctx.newProgressivePromise());
            //写结束标记。
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }else{
            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf,0,fileLength,8192)),ctx.newProgressivePromise());
            //HttpChunkedInput将为我们写结束标记(LastHttpContent)。
            lastContentFuture = sendFileFuture;
        }
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if(total < 0){
                    System.out.println(future.channel()+" 下载进展: "+progress);
                }else{
                    System.out.println(future.channel()+" 下载进展: "+progress+"/"+total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println(future.channel()+" 下载完成！");
            }
        });
        //决定是否关闭连接
        if(!keepAlive){
            //当全部内容写出来时，关闭连接
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
    /**   
     * 设置HTTP响应的日期和缓存头
     * @Author lrh 2020/8/20 9:22
     */
    private void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(HTTP_DATE_FORMAT,Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        //日期标头
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE,dateFormat.format(time.getTime()));
        //添加缓存头
        time.add(Calendar.SECOND,HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES,dateFormat.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL,"private,max-age="+HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED,fileToCache.lastModified());
    }

    /**
     * 设置HTTP响应的内容类型头
     * @Author lrh 2020/8/20 9:18
     */
    private void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,mimetypesFileTypeMap.getContentType(file.getPath()));
    }

    /**   
     * 当文件时间戳与浏览器发送的相同时，发送“304未修改”
     * @Author lrh 2020/8/19 17:31
     */
    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,NOT_MODIFIED,Unpooled.EMPTY_BUFFER);
        this.setDateHeader(response);
        this.sendAndCleanupConnection(ctx,response);
    }
    /**   
     * 设置HTTP响应的日期标头
     * @Author lrh 2020/8/19 17:34
     */
    private void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * 跳转打开文件
     *
     * @Author lrh 2020/8/19 17:24
     */
    private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        this.sendAndCleanupConnection(ctx, response);
    }

    /**
     * 展示文件列表
     *
     * @Author lrh 2020/8/19 17:24
     */
    private void sendListing(ChannelHandlerContext ctx, File dir, String dirPath) {
        StringBuilder buf = new StringBuilder()
                .append("<!DOCTYPE html>\r\n")
                .append("<html><head><meta charset='utf-8' /><title>")
                .append("Listing of: ")
                .append(dirPath)
                .append("</title></head><body>\r\n")
                .append("<h3>Listing of: ")
                .append(dirPath)
                .append("</h3>\r\n")
                .append("<ul>")
                .append("<li><a href=\"../\">上一级</a></li>\r\n");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }
                String name = f.getName();
                if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                    continue;
                }
                buf.append("<li><a href=\"")
                        .append(name)
                        .append("\">")
                        .append(name)
                        .append("</a></li>\r\n");
            }
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = ctx.alloc().buffer(buf.length());
        buffer.writeCharSequence(buf.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        this.sendAndCleanupConnection(ctx, response);
    }

    /**
     * 将uri解析成绝对路径
     *
     * @Author lrh 2020/8/19 17:04
     */
    private static String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }
        //转换文件分隔符
        uri = uri.replace('/', File.separatorChar);
        //简单的安全检查
        if (uri.contains(File.separator + ".") ||
                uri.contains("." + File.separator) ||
                uri.charAt(0) == '.' ||
                uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        //转换为绝对路径。
        return DEFAULT_ROOT_PATH + File.separator + uri;
    }

    /**
     * 发送错误信息
     *
     * @Author lrh 2020/8/19 16:58
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("失败：" + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        this.sendAndCleanupConnection(ctx, response);
    }

    /**
     * 如果Keep-Alive被禁用，则将“Connection: close”头附加到响应
     * 并在发送响应后关闭连接
     *
     * @Author lrh 2020/8/19 16:52
     */
    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response) {
        final FullHttpRequest request = this.fullHttpRequest;
        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            //我们将在响应发送后立即关闭连接，所以我们也应该向客户说清楚。
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ChannelFuture future = ctx.writeAndFlush(response);
        if (!keepAlive) {
            //在发送响应后立即关闭连接。
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
