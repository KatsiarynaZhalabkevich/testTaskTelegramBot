package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.entity.UserState;
import com.example.demo.utils.DateTimeConverter;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        this.chatStates = db.getMap("chatStates");
    }

    public void replyToStart(MessageContext ctx) {
        System.out.println("2. Replay to start");
        SendMessage message = new SendMessage();
        message.setChatId(ctx.chatId());
        message.setText("Hi! This is a bot to notify you about currency changes. Let's make some settings first. Please, enter time to start notifications!");
        sender.execute(message);
        chatStates.put(ctx.chatId(), UserState.AWAITING_TIME_START);

    }

    private void stopChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Thank you for using bot. See you soon!\nPress /start to start using bot again");
        // remove user and
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
        chatStates.remove(chatId);
    }

    public User replyToButtons(long chatId, Message message) {
        System.out.println("2. Reply to buttons");
        if (message.getText().equalsIgnoreCase("/stop")) {
            stopChat(chatId);
            return null;
        }
        System.out.println("Chat states: " + chatStates.entrySet());
        User user = new User();
        switch (chatStates.get(chatId)) {
            case AWAITING_TIME_START -> user.setTimeOfStart(replyToSetTimeStart(chatId, message));
            case AWAITING_TIME_FINISH -> user.setTimeOfFinish(replyToSetTimeFinish(chatId, message));
            case AWAITING_PERCENT -> user.setPercent(replyToSetPercent(chatId, message));
            case SUBSCRIBED -> replyToSubscribed(chatId);
            case UNSUBSCRIBED -> replyToUnsubscribed(chatId);
            default -> unexpectedMessage(chatId);
        }
        System.out.println("2. User: " + user);
        return user;
    }

    public void sendUpdateMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sender.execute(sendMessage);
    }

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("I did not expect that.");
        sender.execute(sendMessage);
    }

    public void replyToSubscribed(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("You've been successfully subscribed for the notifications!");
        sender.execute(sendMessage);
    }

    public void replyToUnsubscribed(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("You've been successfully unsubscribed from the notifications!");
        sender.execute(sendMessage);
    }

    private LocalTime replyToSetTimeStart(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String text = message.getText();

        LocalTime time = DateTimeConverter.parseStringTime(text);

        if (time != null) {
            sendMessage.setText("Great! Enter time to finish:");
            sender.execute(sendMessage);
            chatStates.put(chatId, UserState.AWAITING_TIME_FINISH);
        } else {
            sendMessage.setText("Time is incorrect. Please try again");
            sender.execute(sendMessage);
        }
        return time;
    }



    private LocalTime replyToSetTimeFinish(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String text = message.getText();

        LocalTime time = DateTimeConverter.parseStringTime(text);

        if (time != null) {
            sendMessage.setText("Great! Enter % for notifications:");
            sender.execute(sendMessage);
            chatStates.put(chatId, UserState.AWAITING_PERCENT);
        } else {
            sendMessage.setText("Time is incorrect. Please try again");
            sender.execute(sendMessage);
        }
        return time;
    }

    private Integer replyToSetPercent(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String text = message.getText();
        Integer percent;
        try {
            percent = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            percent = null;
        }

        if (percent != null && percent > 0 && percent < 100) {
            sendMessage.setText("Great! You've been successfully subscribed!");
            sender.execute(sendMessage);
            chatStates.put(chatId, UserState.SUBSCRIBED);

        } else {
            sendMessage.setText("Percent value is incorrect. Please try again");
            sender.execute(sendMessage);
        }
        return percent;
    }


    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }


}
