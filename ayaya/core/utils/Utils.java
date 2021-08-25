package ayaya.core.utils;

import java.util.Map;

import static java.util.Map.entry;

/**
 * Class with static utility methods.
 */
public class Utils {

    public static final String TIMEZONES =
            "ACT - Australia/Darwin\n" +
                    "AET - Australia/Sydney\n" +
                    "AGT - America/Argentina/Buenos_Aires\n" +
                    "ART - Africa/Cairo\n" +
                    "AST - America/Anchorage\n" +
                    "BET - America/Sao_Paulo\n" +
                    "BST - Asia/Dhaka\n" +
                    "CAT - Africa/Harare\n" +
                    "CNT - America/St_Johns\n" +
                    "CST - America/Chicago\n" +
                    "CTT - Asia/Shanghai\n" +
                    "EAT - Africa/Addis_Ababa\n" +
                    "ECT - Europe/Paris\n" +
                    "GMT - Europe/London\n" +
                    "IET - America/Indiana/Indianapolis\n" +
                    "IST - Asia/Kolkata\n" +
                    "JST - Asia/Tokyo\n" +
                    "MIT - Pacific/Apia\n" +
                    "NET - Asia/Yerevan\n" +
                    "NST - Pacific/Auckland\n" +
                    "PLT - Asia/Karachi\n" +
                    "PNT - America/Phoenix\n" +
                    "PRT - America/Puerto_Rico\n" +
                    "PST - America/Los_Angeles\n" +
                    "SST - Pacific/Guadalcanal\n" +
                    "VST - Asia/Ho_Chi_Minh\n" +
                    "ET - UTC-04:00\n" +
                    "EDT - UTC-04:00\n" +
                    "EST - UTC-05:00\n" +
                    "HST - UTC-10:00\n" +
                    "MST - UTC-07:00\n";

    public static final Map<String, String> ZONE_IDS = Map.ofEntries(
            entry("ACT", "Australia/Darwin"),
            entry("AET", "Australia/Sydney"),
            entry("AGT", "America/Argentina/Buenos_Aires"),
            entry("ART", "Africa/Cairo"),
            entry("AST", "America/Anchorage"),
            entry("BET", "America/Sao_Paulo"),
            entry("BST", "Asia/Dhaka"),
            entry("CAT", "Africa/Harare"),
            entry("CNT", "America/St_Johns"),
            entry("CST", "America/Chicago"),
            entry("CTT", "Asia/Shanghai"),
            entry("EAT", "Africa/Addis_Ababa"),
            entry("ECT", "Europe/Paris"),
            entry("GMT", "Europe/London"),
            entry("IET", "America/Indiana/Indianapolis"),
            entry("IST", "Asia/Kolkata"),
            entry("JST", "Asia/Tokyo"),
            entry("MIT", "Pacific/Apia"),
            entry("NET", "Asia/Yerevan"),
            entry("NST", "Pacific/Auckland"),
            entry("PLT", "Asia/Karachi"),
            entry("PNT", "America/Phoenix"),
            entry("PRT", "America/Puerto_Rico"),
            entry("PST", "America/Los_Angeles"),
            entry("SST", "Pacific/Guadalcanal"),
            entry("VST", "Asia/Ho_Chi_Minh"),
            entry("EDT", "-04:00"),
            entry("ET", "-04:00"),
            entry("EST", "-05:00"),
            entry("MST", "-07:00"),
            entry("HST", "-10:00")
    );

    /**
     * Utility method to return the string with the day and respective suffix.
     *
     * @param day the day number
     * @return string
     */
    public static String getDayWithSuffix(int day) {
        if (day / 10 == 1) return day + "th";
        switch (day % 10) {
            case 1:
                return day + "st";
            case 2:
                return day + "nd";
            case 3:
                return day + "rd";
            default:
                return day + "th";
        }
    }

    /**
     * Checks if the whole content of a string can be a valid Long number.
     *
     * @param s the string to test
     * @return true if the string can be a valid long number, false on the contrary
     */
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static final String JPG = ".jpg";
    private static final String PNG = ".png";
    private static final String WEBP = ".webp";
    private static final String GIF = ".gif";

    /**
     * Returns an array with 3 urls for the same image but with different sizes.
     * The first url shows a version that may be larger.
     * The second url is resized to 512x512 for display purposes only.
     *
     * @param originalURL  the original url
     * @param size1        a size for one of the avatar urls
     * @param size2        another size for the other avatar url
     * @return string array with 2 urls
     */
    public static String[] getUrls(String originalURL, int size1, int size2) {
        int minSize = Math.min(size1, size2);
        int maxSize = Math.max(size1, size2);
        String url = originalURL;
        String displayUrl;
        if (!url.endsWith(JPG) || !url.endsWith(PNG) || !url.endsWith(WEBP) || !url.endsWith(GIF))
            url = url.split("\\?size=")[0];
        displayUrl = url + "?size=" + minSize;
        url = url + "?size=" + maxSize;
        return new String[]{url, displayUrl};
    }

    /**
     * Prints a bar giving 2 double values and a lenght.
     * One of the values will be the current value, while the other is the max value.
     *
     * @param value1 one of the values
     * @param value2 other of the values
     * @param length the lenght of the bar
     * @return string with the bar
     */
    public static String printBar(double value1, double value2, int length) {
        double current = Math.min(value1, value2);
        double max = Math.max(value1, value2);
        StringBuilder bar = new StringBuilder();
        long fullChars = Math.round(current / max * length);
        for (long i = 1; i <= length; i++) {
            if (i <= fullChars) bar.append('▓');
            else bar.append('░');
        }
        return bar.toString();
    }

}