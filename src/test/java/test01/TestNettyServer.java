package test01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author lrh 2020/8/6 9:01
 */
public class TestNettyServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(boosGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TestNettyServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.bind(PORT);
            System.out.println("服务器启动成功，监听端口："+PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    static class TestNettyServerHandler extends ChannelInboundHandlerAdapter{
        private static Set<Channel> channels = new HashSet<>();
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("服务器收到的消息："+buf.toString(CharsetUtil.UTF_8));
            Channel self = ctx.channel();
            self.writeAndFlush(Unpooled.copiedBuffer("测试群聊",CharsetUtil.UTF_8));
            for(Channel ch : channels){
                ch.writeAndFlush("哈哈哈哈哈");
            }
        }

        @Override
        public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete");

            ctx.channel().eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                        ctx.writeAndFlush(Unpooled.copiedBuffer("你好呀客户端！2",CharsetUtil.UTF_8));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
           ctx.channel().eventLoop().schedule(new Runnable() {
               @Override
               public void run() {
                   try {
                       TimeUnit.SECONDS.sleep(10);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   ctx.writeAndFlush(Unpooled.copiedBuffer("你好呀客户端！2",CharsetUtil.UTF_8));
               }
           },5,TimeUnit.SECONDS);
            ctx.writeAndFlush(Unpooled.copiedBuffer("你好呀客户端！",CharsetUtil.UTF_8));

        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handlerAdd");
            channels.add(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handlerRemove");
            channels.remove(ctx.channel());
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            cause.printStackTrace();
        }
    }

    static class TestNettyServerHandler2 extends SimpleChannelInboundHandler<String>{
        private static Set<Channel> channels = new HashSet<>();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel channel = ctx.channel();
            ctx.writeAndFlush("[you] 说："+s);
            System.out.println(channel.remoteAddress()+" 说："+s);
            for(Channel ch : channels){
                if(ch != channel){
                    ctx.writeAndFlush("["+channel.remoteAddress()+"] 说："+s);
                }else{
                    ctx.writeAndFlush("[you] 说："+s);
                }
            }

        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            channels.add(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            channels.remove(ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            cause.printStackTrace();
        }
    }

}
