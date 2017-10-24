package dropzone.util;

import java.util.Random;

public class UniqueKeyGenerator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final Random random = new Random();
    private static final char[] buffer = new char[10];

    public static String nextKey() {
        for (int i = 0; i < buffer.length; ++i) {
            char letter = LETTERS.charAt(random.nextInt(LETTERS.length()));
            char digit = DIGITS.charAt(random.nextInt(DIGITS.length()));
            char symbol = random.nextBoolean() ? letter : digit;
            buffer[i] = symbol;
        }
        return new String(buffer);
    }
}