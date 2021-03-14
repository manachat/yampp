package vafilonov.yampp.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ByteAccumulatorTest {

    static byte[] sample;

    @BeforeAll
    static void setSample() {
        sample = "Тестовое предложение\n \0\0\t Test sentence !@#$%^&*()!\"№;%:?*() |||{}[]"
                .getBytes(StandardCharsets.UTF_8);
    }


    @Test
    void testAppend() {
        ByteAccumulator test = new ByteAccumulator();
        test.append(sample);
        assertEquals(sample.length, test.getSize());
        assertArrayEquals(sample, Arrays.copyOf(test.array(), test.getSize()));
        test.clear();

        test.append(sample, 50);
        test.append(sample, 50, sample.length - 50);
        assertEquals(sample.length, test.getSize());
        assertArrayEquals(sample, Arrays.copyOf(test.array(), test.getSize()));
        test.clear();

        test.append(sample, 50, 16);
        assertEquals(16, test.getSize());
        assertArrayEquals(Arrays.copyOfRange(sample, 50, 66), Arrays.copyOf(test.array(), test.getSize()));
        test.clear();

        test.append(sample, 60, 64);
        assertEquals(29, test.getSize());
        assertArrayEquals(Arrays.copyOfRange(sample, 60, 89), Arrays.copyOf(test.array(), test.getSize()));


        assertThrows(IllegalArgumentException.class, () -> test.append(sample, 5000, 5000));
    }
}