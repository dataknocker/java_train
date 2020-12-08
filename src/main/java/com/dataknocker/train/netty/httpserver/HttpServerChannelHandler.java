package com.dataknocker.train.netty.httpserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpServerChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            System.out.println("Uri:" + request.uri());
        }
        if(msg instanceof HttpContent){
            HttpContent content = (HttpContent)msg;
            ByteBuf buf = content.content();
            System.out.println("Receive request: " + buf.toString(CharsetUtil.UTF_8));
            ByteBuf outputBuf = Unpooled.copiedBuffer("hello world", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, outputBuf);
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().add(HttpHeaderNames.CONTENT_LENGTH, outputBuf.readableBytes());
            ctx.writeAndFlush(response);
        }
    }
}
