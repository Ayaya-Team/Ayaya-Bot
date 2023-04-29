package ayaya.core.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The conversion arrays. Each array contains unit strings of a unit type.
 */
public enum ConvertionArrays {

    LENGHTH_UNITS(new ArrayList<>(Arrays.asList("km", "hm", "dam", "m", "dm", "cm", "mm", "mi", "ft"))),
    SPEED_UNITS(new ArrayList<>(Arrays.asList("km/h", "m/s", "mph"))),
    WEIGHT_UNITS(new ArrayList<>(Arrays.asList("kg", "g", "lbs", "oz"))),
    TEMPERATURE_UNITS(new ArrayList<>(Arrays.asList("k", "c", "f"))),
    PRESSURE_UNITS(new ArrayList<>(Arrays.asList("at", "atm", "bar", "mmhg", "pa", "psi"))),
    INFORMATION_UNITS(new ArrayList<>(Arrays.asList("bit", "kbit", "mbit", "gbit", "tbit", "pbit", "ebit", "ybit",
            "byte", "kbyte", "mbyte", "gbyte", "tbyte", "pbyte", "ebyte", "zbyte", "ybyte"))),
    CURRENCIES(new ArrayList<>(Arrays.asList("aud", "bgn", "brl", "cad", "chf", "cny", "czk", "dkk", "eur", "gbp", "hkd",
            "hrk", "huf", "idr", "ils", "inr", "isk", "jpy", "krw", "mxn", "myr", "nok", "nzd", "php", "pln", "ron",
            "rub", "sek", "sgd", "thb", "try", "usd", "zar")));

    private List<String> array;

    ConvertionArrays(ArrayList<String> array) {
        this.array = array;
    }

    public List<String> toList() {
        return array;
    }

}