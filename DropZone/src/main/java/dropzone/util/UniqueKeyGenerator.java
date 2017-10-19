package dropzone.util;

import java.util.Random;

public class UniqueKeyGenerator {

    private static final String letters = "abcdefghijklmnopqrstuvwxyz";
    private static final String digits = "0123456789";
    private static final Random random = new Random();
    private static final char[] buffer = new char[10];
    /**
     * Generate a random string.
     */
    public static String nextKey() {
        for (int i = 0; i < buffer.length; ++i) {
            char letter = letters.charAt(random.nextInt(letters.length()));
            char digit = digits.charAt(random.nextInt(digits.length()));
            char symbol = random.nextBoolean() ? letter : digit;
            buffer[i] = symbol;
        }
        return new String(buffer);
    }
}