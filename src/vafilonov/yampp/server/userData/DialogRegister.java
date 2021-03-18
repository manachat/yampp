package vafilonov.yampp.server.userData;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DialogRegister {

    private final ConcurrentHashMap<Long, Dialog> dialogRegister = new ConcurrentHashMap<>();

    public Dialog 


    private static class Dialog {

        private int lowerKey;
        private int upperKey;

        private long dialogId;

        public Dialog(int firstKey, int secondKey) {
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
            dialogId = dialogId | lowerKey;
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
