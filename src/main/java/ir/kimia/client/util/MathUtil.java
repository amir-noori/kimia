package ir.kimia.client.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {

    public static Double round(Double number) {
        return round(0, number);
    }

    public static Double round(int scale, Double number) {
        BigDecimal bigDecimal = BigDecimal.valueOf(number);
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}
