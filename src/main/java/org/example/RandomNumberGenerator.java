package org.example;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomNumberGenerator {
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateRandomNumber(StringBuilder sb) {
        Random random = new Random();
//        StringBuilder sb = new StringBuilder(16);
        Set<Character> usedChars = new HashSet<>();

        while (sb.length() < 16) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);

            if (!usedChars.contains(randomChar)) {
                sb.append(randomChar);
                usedChars.add(randomChar);
            }
        }

        return sb.toString();
    }
}
