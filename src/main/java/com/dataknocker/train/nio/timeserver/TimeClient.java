package com.dataknocker.train.nio.timeserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClient {
    public static void main(String[] args) throws IOException {
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.socket().setReuseAddress(true);
        clientChannel.socket().setReceiveBufferSize(1024*1024);
        clientChannel.socket().setSendBufferSize(1024*1024);
        boolean connected = clientChannel.connect(new InetSocketAddress(8100));
        Selector selector = Selector.open();
        if(connected) {
            clientChannel.register(selector, SelectionKey.OP_READ);
        } else {
            clientChannel.register(selector, SelectionKey.OP_CONNECT);
        }
        while(true){
            selector.select(1000);
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(!key.isValid()){
                    continue;
                }
                SocketChannel sc = (SocketChannel)key.channel();
                if(key.isConnectable()){
                    if(sc.finishConnect()){
                        sc.register(selector, SelectionKey.OP_READ);
                        String msg = "hello, time.";
                        byte[] reqBytes = msg.getBytes();
                        ByteBuffer reqBuffer = ByteBuffer.allocate(reqBytes.length);
                        reqBuffer.put(reqBytes);
                        reqBuffer.flip();
                        sc.write(reqBuffer);
                    } else{
                        System.out.println("连接失败，退出.");
                        System.exit(-1);
                    }

                }else if(key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
                    int bytes = sc.read(buffer);
                    if(bytes > 0) {
                        buffer.flip();
                        byte[] reqBytes = new byte[buffer.remaining()];
                        buffer.get(reqBytes);
                        String msg = new String(reqBytes, "UTF-8");
                        System.out.println("Receive msg from server:" + msg);
                        System.exit(0);
                    }


                }
            }
        }
    }
}
