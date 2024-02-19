package me.dwyur.newsplugin.utility;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringUtils {
    public String removeUnsupportedCharacters(String input) {
        Pattern emojiPattern = Pattern.compile("[\\s]*+[\\p{So}\\p{Sk}\\p{Sm}]");
        Matcher emojiMatcher = emojiPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (emojiMatcher.find()) {
            emojiMatcher.appendReplacement(buffer, "");
        }
        emojiMatcher.appendTail(buffer);

        return buffer.toString().replaceFirst("^\\s+", "");
    }

    public static List<String> splitStringByLength(String input, int maxLength) {
        List<String> result = new ArrayList<>();

        if (input == null || input.isEmpty() || maxLength <= 0) {
            return result;
        }

        int inputLength = input.length();
        int chunks = (int) Math.ceil((double) inputLength / maxLength);

        for (int i = 0; i < chunks; i++) {
            int startIndex = i * maxLength;
            int endIndex = Math.min(startIndex + maxLength, inputLength);
            result.add(input.substring(startIndex, endIndex));
        }

        return result;
    }
}
