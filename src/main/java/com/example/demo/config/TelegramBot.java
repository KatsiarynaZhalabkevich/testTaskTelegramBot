package com.example.demo.config;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class TelegramBot extends AbilityBot {
    @Value("${telegram-bot.apiKey}")
    private String apiKey;
    private ResponseHandler responseHandler;
    @Autowired
    private UserService userService;

    @Autowired
    public TelegramBot(Environment env) {
        super(env.getProperty("telegram-bot.apiKey"), "currency_change_test_bot");
        responseHandler = new ResponseHandler(silent, db);
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    responseHandler.replyToStart(ctx.chatId());
                    userService.createUser(ctx.user().getUserName(), ctx.chatId());
                })
                .build();
    }


    @Override
    public long creatorId() {
        return 1L;
    }


    public void sendUpdateMessage(Long chatId, String s) {
        responseHandler.sendUpdateMessage(chatId, s);
    }
}