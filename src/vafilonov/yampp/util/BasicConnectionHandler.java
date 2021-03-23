package vafilonov.yampp.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

public abstract class BasicConnectionHandler implements Runnable {

    protected static final int BUFFER_SIZE = 1024;


    //TODO добавить к чтению и записи длину сообщения в первые n байт
    protected static final int msgLenInBytes = 4;   // messages with int bytes...

    protected final ByteBuffer lenBuf = ByteBuffer.allocate(msgLenInBytes);

    protected String readMessageFromNetChannel(ByteBuffer buf, SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();

        channel.read(lenBuf);
        int len = lenBuf.getInt();
        final ByteAccumulator accumulator = new ByteAccumulator(len);

        while (len >= BUFFER_SIZE) {
            channel.read(buf);
            len -= BUFFER_SIZE;
            buf.flip();
            accumulator.append(buf.array(), buf.remaining());
            buf.clear();
        }
        ByteBuffer remainder = ByteBuffer.allocate(len);
        channel.read(remainder);
        accumulator.append(remainder.array());
        System.err.println("Net channel read");
        String extracted = new String(accumulator.array(), 0, accumulator.getSize(), StandardCharsets.UTF_8);
        System.err.println(extracted);
        return extracted;
    }

    protected void sendMessageThroughNetChannel(String message, SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        int len = message.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer send = ByteBuffer.allocate(msgLenInBytes + len);
        send.putInt(len);
        send.put(message.getBytes(StandardCharsets.UTF_8));
        System.err.println(new String(send.array()));
        channel.write(send);
        System.err.println("sent through net channel");
    }

    protected void sendMessageThroughNetChannel(String message, SelectionKey key, ZonedDateTime time) throws IOException{
        String timedMessage = message + Constants.TOKEN_SEPARATOR + time;
        sendMessageThroughNetChannel(timedMessage, key);
    }
}
