package com.dataknocker.train.nio.timeserver;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8100;
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("TimeServer started in port:" + port);
        SocketChannel socketChannel = null;
        while (true) {
            selector.select(1000);
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if(!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel)key.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024*1024);
                    int bytes = channel.read(readBuffer);
                    if(bytes > 0){
                        readBuffer.flip();
                        byte[] request = new byte[readBuffer.remaining()];
                        readBuffer.get(request);
                        String requestBody = new String(request, "UTF-8");
                        System.out.println("Receive msg from client: " + requestBody);
                        String response = "hello, time.".equalsIgnoreCase(requestBody) ? System.currentTimeMillis() + "" : "Bad Order";
                        byte[] res = response.getBytes();
                        ByteBuffer resBuffer = ByteBuffer.allocate(res.length);
                        resBuffer.put(res);
                        resBuffer.flip();
                        channel.write(resBuffer);
                    }else if(bytes < 0){
                        key.cancel();
                        channel.close();
                    }
                }
            }
        }
    }
}
