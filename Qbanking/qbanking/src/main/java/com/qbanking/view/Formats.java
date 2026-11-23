package com.qbanking.view;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** How numbers and dates are written on screen. */
final class Formats {

    static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Formats() {
    }

    /** 12345.5 becomes "12,345.50". */
    static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
