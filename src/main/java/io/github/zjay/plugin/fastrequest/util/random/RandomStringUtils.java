package io.github.zjay.plugin.fastrequest.util.random;

public class RandomStringUtils {

    private static final String alphabets = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String randomAlphabetic(int count){
        if(count <= 0){
            throw new IllegalArgumentException("count need > 0");
        }
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < count; i++) {
            // generate a random number between 0 and length of all characters
            int randomIndex = (int)(Math.random() * alphabets.length());
            // retrieve character at index and add it to result
            randomString.append(alphabets.charAt(randomIndex));
        }
        return randomString.toString();
    }

}
