package com.example.demo.service;

import com.example.demo.config.TelegramBot;
import com.example.demo.entity.User;
import com.example.demo.repo.UserRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private TelegramBot telegramBot;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void notifyUsers(Map<String, Double> changes) {
        //TODO add pageble and findBy start and End time in future
        if (ObjectUtils.isNotEmpty(changes)) {
            String notificationMessage = createNotificationMessage(changes);
            List<User> users = userRepository.findAll();
            users.forEach(user -> {
telegramBot.sendUpdateMessage(user.getChatId(), "Hey! There is an info about the currency changes: \n" + notificationMessage);
                        ;
            });
        }

    }

    private String createNotificationMessage(Map<String, Double> changes) {
     return changes.keySet().stream()
                .map(key -> key + " -> " + changes.get(key))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public void createUser(String userName, Long chatId) {
        User user = new User();
        user.setUsername(userName);
        user.setChatId(chatId);
        userRepository.save(user);
    }
}
