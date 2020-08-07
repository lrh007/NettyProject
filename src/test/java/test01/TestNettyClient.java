package test01;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

/**
 * @Author lrh 2020/8/6 9:44
 */
public class TestNettyClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TestNettyClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            System.out.println("客户端启动成功，连接服务器成功！");
            Scanner scanner = new Scanner(System.in);
            while (true){
                String s = scanner.nextLine();
                if("exit".equals(s)){
                    break;
                }
                future.channel().writeAndFlush(Unpooled.copiedBuffer(s,CharsetUtil.UTF_8));
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    static class TestNettyClientHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("服务器返回："+buf.toString(CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            cause.printStackTrace();
        }
    }
    static class TestNettyClientHandler2 extends SimpleChannelInboundHandler<String>{


        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
            System.out.println("服务器返回："+s);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            cause.printStackTrace();
        }
    }
}
