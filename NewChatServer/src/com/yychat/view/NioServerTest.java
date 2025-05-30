package com.yychat.view;

import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.socket.nio.NioServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class NioServerTest extends Thread {
    static Set<SocketChannel> socketChannels = new HashSet<>();
    public static void doWrite(SocketChannel channel, String response) throws IOException {
        response = "收到消息：" + response;
        //将缓冲数据写入渠道，返回给客户端
        channel.write(BufferUtil.createUtf8(response));
    }
    NioServerTest(){

    }

    @Override
    public void run() {
        NioServer server = new NioServer(3456);
        server.setChannelHandler((sc)->{
            if(!socketChannels.contains(sc)){
                socketChannels.add(sc);
            }
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            try{
                //从channel读数据到缓冲区
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    //Flips this buffer.  The limit is set to the current position and then
                    // the position is set to zero，就是表示要从起始位置开始读取数据
                    readBuffer.flip();
                    //eturns the number of elements between the current position and the  limit.
                    // 要读取的字节长度
                    byte[] bytes = new byte[readBuffer.remaining()];
                    //将缓冲区的数据读到bytes数组
                    readBuffer.get(bytes);
                    String body = StrUtil.utf8Str(bytes);
                    Console.log("[{}]: {}", sc.getRemoteAddress(), body);
                    for(SocketChannel channel : socketChannels){
                        doWrite(channel, body);
                    }
                } else if (readBytes < 0) {
                    IoUtil.close(sc);
                }
            } catch (IOException e){
                throw new IORuntimeException(e);
            }
        });
        server.listen();
    }
}
