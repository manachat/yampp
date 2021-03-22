package vafilonov.yampp.server.userData;

import vafilonov.yampp.util.TimedMessage;

import java.sql.Time;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DialogRegister {

    private final ConcurrentHashMap<Long, Dialog> dialogRegister = new ConcurrentHashMap<>();


    public Dialog getDialog(int key1, int key2) {
        if (key1 == key2) {
            throw new IllegalArgumentException("Keys are equal.");
        }
        Dialog d;
        long key;
        if (key1 < key2) {
            key = (long) key1 << 32;
            key = key | (long) key2;
        } else {
            key = (long) key2 << 32;
            key = key | (long) key1;
        }

        d = dialogRegister.get(key);
        if (d == null) {
            Dialog newDialog = new Dialog(key1, key2);
            dialogRegister.put(newDialog.dialogId, newDialog);
            d = newDialog;
        }

        return d;
    }



    static class Dialog {

        private final int lowerKey;
        private final int upperKey;

        private long dialogId;

        private final Queue<TimedMessage> upToDownMessages = new ArrayDeque<>();
        private final Queue<TimedMessage> downToUpMessages = new ArrayDeque<>();

        private final Deque<TimedMessage> history = new ArrayDeque<>();

        synchronized void putMessage(int sender, TimedMessage message) {
            history.addLast(message);
            if (sender == lowerKey) {
                downToUpMessages.offer(message);
            } else {
                upToDownMessages.offer(message);
            }
        }

        synchronized TimedMessage getMessage(int receiver) {

            if (receiver == lowerKey) {
                return upToDownMessages.poll();
            } else {
                return downToUpMessages.poll();
            }
        }

        private Dialog(int firstKey, int secondKey) {
            if (firstKey == secondKey) {
                throw new IllegalArgumentException("Equal keys are not allowed.");
            }


            if (firstKey > secondKey) {
                lowerKey = firstKey;
                upperKey = secondKey;
            } else {
                lowerKey = secondKey;
                upperKey = firstKey;
            }

            dialogId = (long) upperKey << 32;
            dialogId = dialogId | (long)lowerKey;
        }

        public long getDialogId() {
            return dialogId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dialog dialog = (Dialog) o;
            return lowerKey == dialog.lowerKey && upperKey == dialog.upperKey;
        }

        @Override
        public int hashCode() {
            return (int) dialogId;
        }
    }
}
