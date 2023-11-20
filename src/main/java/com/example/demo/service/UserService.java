package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.utils.DateTimeConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsersToNotify() {
        int hours = LocalDateTime.now().getHour();
        int minutes = LocalDateTime.now().getMinute();

        // create LocalDateTime
        String time = String.valueOf(hours).concat(":").concat(String.valueOf(minutes));
        LocalTime currentTime = DateTimeConverter.parseStringTime(time);

       return userRepository.findAllByTimeOfStartIsAfterAndTimeOfFinishIsBefore(currentTime, currentTime);
       // return userRepository.findAll();
    }


    public void createUser(Long telegramUserId, String userName, Long chatId) {
        Optional<User> userOpt = userRepository.findByTelegramUserId(telegramUserId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            System.out.println("New user!");
            user = new User();
            user.setTelegramUserId(telegramUserId);
            user.setUsername(userName);
        }
        user.setChatId(chatId);
        userRepository.save(user);
    }

    public void updateUser(User user) {
        System.out.println("User is ready to update!");
        Optional<User> userOpt = userRepository.findByChatId(user.getChatId());
        if (userOpt.isPresent()) {
            System.out.println("User present in DB");
            User dbUser = prepareUserToUpdate(userOpt.get(), user);

            userRepository.save(dbUser);
        }


    }

    private User prepareUserToUpdate(User dbUser, User updUser) {
        LocalTime timeToStart = updUser.getTimeOfStart();
        LocalTime timeToFinish = updUser.getTimeOfFinish();
        Integer percent = updUser.getPercent();

        if (timeToStart != null) {
            dbUser.setTimeOfStart(timeToStart);
        }
        if (timeToFinish != null) {
            dbUser.setTimeOfFinish(timeToFinish);
        }
        if (percent != null) {
            dbUser.setPercent(percent);
        }
        return dbUser;
    }
}
