package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class TelegramBot extends AbilityBot {
    private final ResponseHandler responseHandler;
    @Autowired
    private UserService userService;

    @Autowired
    public TelegramBot(Environment env) {
        super(env.getProperty("telegram-bot.apiKey"), "currency_change_test_bot");
        responseHandler = new ResponseHandler(silent, db);
    }

    public Ability startBot() {
        System.out.println("1. Start Bot");
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    responseHandler.replyToStart(ctx);
                    userService.createUser(ctx.user().getId(), ctx.user().getUserName(), ctx.chatId());
                })
                .build();
    }

    public Reply replyToButtons() {
        System.out.println("1. Reply to Buttons");
        BiConsumer<BaseAbilityBot, Update> action = (abilityBot, upd) -> {
            User user = responseHandler.replyToButtons(getChatId(upd), upd.getMessage());
            System.out.println("1. User. Reply buttons: " + user);
            if (ObjectUtils.isEmpty(user)) {
                return;
            }
            user.setChatId(getChatId(upd));
            userService.updateUser(user);
        };
        return Reply.of(action, Flag.TEXT, upd -> responseHandler.userIsActive(getChatId(upd)));
    }


    @Override
    public long creatorId() {
        return 1L;
    }


    public void sendUpdateMessage(Long chatId, String s) {
        responseHandler.sendUpdateMessage(chatId, s);
    }
}