package com.dataknocker.train.nio.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NioSendClient {
    public static void main(String[] args) throws IOException {
        File sourceFile = new File(NioSendClient.class.getResource("/data/source").getFile());
        FileChannel fileChannel = new FileInputStream(sourceFile).getChannel();

        SocketChannel clientChannel =SocketChannel.open();
        clientChannel.configureBlocking(false);

        clientChannel.connect(new InetSocketAddress(8100));
        while(!clientChannel.finishConnect()){}
        System.out.println("Client connect server success.");
        System.out.println("Begin send file.");
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer fileNameBuffer = charset.encode("target");
        clientChannel.write(fileNameBuffer);
        ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
        buffer.putLong(sourceFile.length());
        buffer.flip();
        clientChannel.write(buffer);
        buffer.clear();
        int length = 0;
        long progress = 0;
        while((length = fileChannel.read(buffer))>0){
            buffer.flip();
            clientChannel.write(buffer);
            buffer.clear();
            progress += length;
            System.out.println("Progress:"+ (100*progress/sourceFile.length()) + "%...");
        }
        System.out.println("Finish send file.");
    }
}
