package com.example.demo.service;

import com.example.demo.config.TelegramBot;
import com.example.demo.entity.User;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationScheduler {
    private final CurrencyService currencyService;
    private final UserService userService;
    private final TelegramBot bot;
    private Map<String, BigDecimal> prevCurrencyValues = new HashMap<>();

    public NotificationScheduler(CurrencyService currencyService, UserService userService, TelegramBot bot) {
        this.currencyService = currencyService;
        this.userService = userService;
        this.bot = bot;
    }

    @Scheduled(cron = "${currency-change.upd.cron}")
    public void getCurrencyChanges() {
        System.out.println("Scheduler works...");
        Map<String, BigDecimal> currentCurrencyValues = currencyService.getCurrency();;
        Map<String, Double> changes = new HashMap<>();
        if (ObjectUtils.isNotEmpty(prevCurrencyValues)) {
            System.out.println("Get new currency value");

            Set<String> symbols = prevCurrencyValues.keySet();
            symbols.stream()
                    .filter(s -> prevCurrencyValues.get(s).compareTo(currentCurrencyValues.get(s)) != 0)
                    .forEach(s -> calculateCurrencyChange(changes, s, prevCurrencyValues.get(s), currentCurrencyValues.get(s)));
            if (ObjectUtils.isNotEmpty(changes)) {
                System.out.println("There are some changes in currency");
                // get users by parameters
                List<User> users = userService.getUsersToNotify();
                System.out.println("Users to notify: " + users.size());
                // notify by bot
                notifyUsers(changes, users);
            }
        }

        prevCurrencyValues = currentCurrencyValues;
    }

    private void notifyUsers(Map<String, Double> changes, List<User> users) {
        //TODO add pageble and findBy start and End time in future
        if (ObjectUtils.isNotEmpty(changes)) {
            String notificationMessage = createNotificationMessage(changes);
            System.out.println("Message lengh: " + notificationMessage.length());
            // Max len 4096

            users.forEach(user -> {
                System.out.println("Notify User: " + user);
                bot.sendUpdateMessage(user.getChatId(),
                        "Hey! There is an info about the currency changes: ");
                bot.sendUpdateMessage(user.getChatId(), notificationMessage);
            });
        }

    }

    private String createNotificationMessage(Map<String, Double> changes) {
        return changes.keySet().stream()
                .map(key -> key + " -> " + changes.get(key))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private void calculateCurrencyChange(Map<String, Double> changes, String symbol, BigDecimal prevVal,
                                         BigDecimal curVal) {

        BigDecimal result = (prevVal.divide(curVal, 2, RoundingMode.HALF_EVEN)
                .subtract(new BigDecimal(1)))
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN);
        double res = result.setScale(0, RoundingMode.HALF_EVEN).abs().doubleValue();
        // collect all possible changes
        changes.put(symbol, res);

    }
}
