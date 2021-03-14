package vafilonov.yampp.util;

import java.util.Arrays;

public class ByteAccumulator {

    private byte[] array;

    private int size;

    public ByteAccumulator() {
        this(16);
    }

    public ByteAccumulator(int n) {
        array = new byte[n];
        size = 0;
    }

    public void append(byte[] arr) {
        if (arr == null) {
            throw new NullPointerException();
        }
        if (arr.length == 0) {
            return;
        }

        if (arr.length > array.length - size) {
            extend();
        }

        for (int i = 0; i < arr.length; i++, size++) {
            array[size] = arr[i];
        }
    }

    public void clear() {
        size = 0;
        if (array.length > 32) {
            cut();
        }
    }

    public void reset(int newSize) {
        array = new byte[newSize];
        size = 0;
    }

    private void extend() {
        int newCapacity = array.length * 2;
        array = Arrays.copyOf(array, newCapacity);
    }

    private void cut() {
        int newCapacity = array.length / 2;
        array = Arrays.copyOf(array, newCapacity);
    }

    public byte[] array() {
        return array;
    }

}
