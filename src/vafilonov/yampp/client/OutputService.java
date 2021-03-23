package vafilonov.yampp.client;

import vafilonov.yampp.util.TimedMessage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

class OutputService implements Runnable{


    BlockingQueue<TimedMessage> outQueue = new ArrayBlockingQueue<>(10);

    @Override
    public void run() {
        try {
            while (true) {
                TimedMessage message = outQueue.take();
                if (message.getMessage() == null) {
                    System.out.println();
                } else if (message.getTime() == null) {
                    System.out.println(message.getMessage());
                } else {
                    System.out.println(message.getTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + ":" + message.getMessage());
                }
            }
        } catch (InterruptedException interex) {
            System.out.println("Out thread interrupted exception");
        }
    }

    public synchronized void submitMessage(TimedMessage message) {
        outQueue.offer(message);
    }

    public synchronized void submitMessage(String message) {
        outQueue.offer(new TimedMessage(message, ZonedDateTime.now()));
    }

    public synchronized void nextLine() {
        outQueue.offer(new TimedMessage(null, ZonedDateTime.now()));
    }

    public synchronized void serviceMessage(String message) {
        outQueue.offer(new TimedMessage(message, null));
    }
}
