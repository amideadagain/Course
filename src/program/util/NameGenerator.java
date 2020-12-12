package program.util;

import java.util.Random;

public class NameGenerator {
    private static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String numbers = "1234567890";
    private static final String specials = "_-";
    private static final String[] easterEggs = {"mine", "virus", "deleteProcess", "not mine", "degradation", "stonks",
            "DestroyPolitech", "tor", "notVirus", "definitelyNotVirus", "makeAmericaGreatAgain", "makeUkraineGreatAgain",
            "suicide", "DevideByZero", "doNothing", "WasteMyLifeTryingToUnderstandMyPurpose", "Procrastinate", "crash",
            "notExe", "SkyNet"};

    private static final Random m_random = new Random();

    public static String generate() {
        String result = "";

        if (m_random.nextInt(5) == 0) {
            result += easterEggs[m_random.nextInt(easterEggs.length)];
        } else {
            int nameLength = m_random.nextInt(7) + 5;

            for (int i = 0; i < nameLength; i++) {
                int r = m_random.nextInt(10);
                result += r == 0 ? getRandomSpecial() : r < 6 ? getRandomLetter() : getRandomNumber();
            }
        }

        result += ".exe";

        return result;
    }

    public static String getRandomLetter() {
        return "" + (letters.toCharArray()[m_random.nextInt(letters.length())]);
    }

    public static String getRandomNumber() {
        return "" + (numbers.toCharArray()[m_random.nextInt(numbers.length())]);
    }

    public static String getRandomSpecial() {
        return "" + (specials.toCharArray()[m_random.nextInt(specials.length())]);
    }
}

