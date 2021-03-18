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

    protected String readMessageFromNetChannel(ByteBuffer buf, SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        int read = channel.read(buf);

        final ByteAccumulator accumulator = new ByteAccumulator(read);

        while (read != -1) {
            buf.flip();
            accumulator.append(buf.array(), buf.remaining());
            buf.clear();
            read = channel.read(buf);
        }

        return new String(accumulator.array(), 0, accumulator.getSize(), StandardCharsets.UTF_8);
    }

    protected void sendMessageThroughNetChannel(String message, SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer send = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        channel.write(send);
    }

    protected void sendMessageThroughNetChannel(String message, SelectionKey key, ZonedDateTime time) throws IOException{
        String timedMessage = message + Constants.TOKEN_SEPARATOR + time;
        sendMessageThroughNetChannel(timedMessage, key);
    }
}
