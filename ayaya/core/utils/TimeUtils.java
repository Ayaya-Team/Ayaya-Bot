package ayaya.core.utils;

import java.util.Map;

import static java.util.Map.entry;

public class TimeUtils {

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

}