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
        append(arr, 0, arr.length);
    }

    public void append(byte[] arr, int size) {
        append(arr, 0, size);
    }

    public void append(byte[] arr, int offset, int length) {
        if (arr == null) {
            throw new NullPointerException();
        }
        if (arr.length == 0) {
            return;
        }
        if (offset >= arr.length) {
            throw new IllegalArgumentException("Offset bigger than length.");
        }

        int limit = Math.min(offset + length, arr.length);

        if (limit - offset > array.length - size) {
            extend(limit - offset);
        }

        for (int i = offset; i < limit; i++, size++) {
            array[size] = arr[i];
        }
    }

    public void clear() {
        size = 0;
        int newLen = Math.min(32, array.length);
        array = new byte[newLen];
    }

    public void reset(int newSize) {
        array = new byte[newSize];
        size = 0;
    }

    private void extend(int insertionSize) {
        int needed = size + insertionSize;
        int extensionSize = size;
        int newCapacity = needed + extensionSize;
        array = Arrays.copyOf(array, newCapacity);
    }

    private void cut() {
        int newCapacity = array.length / 2;
        array = Arrays.copyOf(array, newCapacity);
    }

    public byte[] array() {
        return array;
    }

    public int getSize() {
        return size;
    }

}
