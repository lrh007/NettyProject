package com.lrh.netty.http.portunification;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.List;

/**动态操作当前管道以切换协议或启用，SSL or GZIP.
 * @Author lrh 2020/9/3 14:37
 */
public class PortUnificationServerHandler extends ByteToMessageDecoder {
    private final SslContext sslCtx;
    private final boolean detectSsl;
    private final boolean detectGzip;

    public PortUnificationServerHandler(SslContext sslCtx) {
        this(sslCtx,true,true);
    }

    public PortUnificationServerHandler(SslContext sslCtx, boolean detectSsl, boolean detectGzip) {
        this.sslCtx = sslCtx;
        this.detectSsl = detectSsl;
        this.detectGzip = detectGzip;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        //将使用前五个字节来检测协议,字节数必须大于5
        if(in.readableBytes() < 5){
            System.out.println("协议不正确，读取到的字节数="+in.readableBytes());
            return;
        }
        if(isSsl(in)){
            enableSsl(ctx);//启用ssl
        }else{
            final int magic1 = in.getUnsignedByte(in.readerIndex());
            final int magic2 = in.getUnsignedByte(in.readerIndex()+1);
            if (isGzip(magic1, magic2)) {
                enableGzip(ctx); //启用gzip
            }else if(isHttp(magic1,magic2)){
                switchToHttp(ctx); //启用http
            }else{
                //未知协议;丢弃所有内容并关闭连接。
                System.out.println("未知协议;丢弃所有内容并关闭连接。");
                in.clear();
                ctx.close();
            }
        }
    }


    /**
     * 判断是否使用SSL
     * @Author lrh 2020/9/3 14:49
     */
    private boolean isSsl(ByteBuf in){
        if(detectSsl){
            //判断是否被加密
            return SslHandler.isEncrypted(in);
        }
        return false;
    }
    /**   
     * 判断是否使用Gzip
     * @Author lrh 2020/9/3 15:12
     */
    private boolean isGzip(int magic1,int magic2){
        if(detectGzip){
            return magic1 == 31 && magic2 == 139;
        }
        return false;
    }
    /**   
     * 判断是否是http
     * @Author lrh 2020/9/3 15:22
     */
    private boolean isHttp(int magic1,int magic2){
        return
                magic1 == 'G' && magic2 == 'E' || // GET
                magic1 == 'P' && magic2 == 'O' || // POST
                magic1 == 'P' && magic2 == 'U' || // PUT
                magic1 == 'H' && magic2 == 'E' || // HEAD
                magic1 == 'O' && magic2 == 'P' || // OPTIONS
                magic1 == 'P' && magic2 == 'A' || // PATCH
                magic1 == 'D' && magic2 == 'E' || // DELETE
                magic1 == 'T' && magic2 == 'R' || // TRACE
                magic1 == 'C' && magic2 == 'O';   // CONNECT
    }
    /**   
     * 启用ssl
     * @Author lrh 2020/9/3 15:01
     */
    private void enableSsl(ChannelHandlerContext ctx){
        ChannelPipeline p = ctx.pipeline();
        p.addLast("ssl",sslCtx.newHandler(ctx.alloc()));
        p.addLast("unificationA",new PortUnificationServerHandler(sslCtx,false,detectGzip));
        p.remove(this);
    }
    /**
     * 启用Gzip
     * @Author lrh 2020/9/3 15:16
     */
    private void enableGzip(ChannelHandlerContext ctx){
        ChannelPipeline p = ctx.pipeline();
        p.addLast("gzipdeflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        p.addLast("gzipinflater",ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        p.addLast("unificationB",new PortUnificationServerHandler(sslCtx,detectSsl,false));
        p.remove(this);
    }
    /**   
     * 启用http
     * @Author lrh 2020/9/3 15:30
     */
    private void switchToHttp(ChannelHandlerContext ctx){
        ChannelPipeline p = ctx.pipeline();
        p.addLast("decoder",new HttpRequestDecoder()); //解码
        p.addLast("encoder",new HttpResponseEncoder()); //编码
        p.addLast("deflater",new HttpContentCompressor());//压缩内容
//        p.addLast("handler",new HttpSnoopServerHandler());//业务处理
        p.addLast("handler",new HttpSnoopHandler());//业务处理
        p.remove(this);
    }
}
