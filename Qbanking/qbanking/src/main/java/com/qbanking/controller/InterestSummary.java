package com.qbanking.controller;

import java.math.BigDecimal;
import java.time.YearMonth;

/** The result of one monthly interest run, shown to the staff member. */
public record InterestSummary(YearMonth month, int accountsPaid, int accountsSkipped, BigDecimal totalPaid) {
}
