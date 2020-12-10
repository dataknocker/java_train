package com.dataknocker.train.nio.filetransfer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NioReceiveServer {
    public static void main(String[] args) throws IOException {
        Map<SocketChannel, Client> clientMap = new HashMap<>();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8100));
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
        Charset charset = Charset.forName("UTF-8");
        while(selector.select() > 0){
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while(iterator.hasNext()){
                buffer.clear();
                SelectionKey key = iterator.next();
                iterator.remove();
                if(!key.isValid()){
                    continue;
                }
                SocketChannel channel = (SocketChannel)key.channel();
                if(key.isAcceptable()){
                    Client client = new Client();
                    client.address = channel.getRemoteAddress().toString();
                    clientMap.put(channel, client);
                    System.out.println("Client:" + client.address + " connected.");
                }else if(key.isReadable()){
                    Client client = clientMap.get(channel);
                    if(client == null){
                        System.out.println("invalid client");
                        continue;
                    }
                    int num = 0;
                    while((num = channel.read(buffer)) > 0) {
                        buffer.flip();
                        if (null == client.filename) {
                            client.filename = charset.decode(buffer).toString();
                            File dir = new File("data");
                        }
                    }
                }
                key.cancel();
            }
        }
    }
}

class Client{
    public String address;
    public String filename;
    public long length;
}
