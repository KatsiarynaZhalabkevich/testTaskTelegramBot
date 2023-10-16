package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationScheduler {
    private final CurrencyService currencyService;
    private final UserService userService;
    private Map<String, BigDecimal> prevCurrencyValues = new HashMap<>();
    @Value("${currency-change.percent}")
    private int percent;

    public NotificationScheduler(CurrencyService currencyService, UserService userService) {
        this.currencyService = currencyService;
        this.userService = userService;
    }

    @PostConstruct
    void init() {
        prevCurrencyValues.putAll(currencyService.getCurrency());
    }

    @Scheduled(cron = "${currency-change.upd.cron}")
    public void getCurrencyChanges() {
        Map<String, BigDecimal> currentCurrencyValues;
        Map<String, Double> changes = new HashMap<>();
        if (ObjectUtils.isNotEmpty(prevCurrencyValues)) {
            currentCurrencyValues = currencyService.getCurrency();

            Set<String> symbols = prevCurrencyValues.keySet();
            symbols.stream()
                    .filter(s -> prevCurrencyValues.get(s).compareTo(currentCurrencyValues.get(s)) != 0)
                    .forEach(s -> calculateCurrencyChange(changes, s, prevCurrencyValues.get(s), currentCurrencyValues.get(s)));
            if (ObjectUtils.isNotEmpty(changes)) {
                userService.notifyUsers(changes);
            }
        }
    }

    private void calculateCurrencyChange(Map<String, Double> changes, String symbol, BigDecimal prevVal,
                                         BigDecimal curVal) {
        BigDecimal result = (prevVal.divide(curVal).subtract(new BigDecimal(1)))
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
        double res = result.setScale(0, RoundingMode.HALF_EVEN).abs().doubleValue();
        if (res >= percent) {
            changes.put(symbol, res);
        }

    }
}
