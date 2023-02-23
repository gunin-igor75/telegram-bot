package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.service.ServiceTask;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final TelegramBot telegramBot;

    private final ServiceTask service;

    private final static Logger LOGGER = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static String START = "/start";

    private static String PATTERN = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)";

    public TelegramBotUpdatesListener(TelegramBot telegramBot, ServiceTask service) {
        this.telegramBot = telegramBot;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            LOGGER.info("Processing update: {}", update);
            String message = update.message().text();
            if (message != null) {
                long idChat = update.message().chat().id();
                distributionMessage(idChat, message);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void distributionMessage(long idChat, String message) {
        if (message.equals(START)) {
            LOGGER.info("Answer start {}", idChat);
            sendMessage(idChat, "Привет");
            return;
        }
        List<Task> listTask = parceMessage(idChat, message);
        if (listTask.isEmpty()) {
            LOGGER.info("String does not contain valid tasks {}", message);
        } else {
            LOGGER.info("Save task");
            for (Task task : listTask) {
                try {
                    service.addTask(task);
                } catch (Exception ex) {
                    LOGGER.warn("Not correct data duplicate");
                }
            }
        }
    }

    private List<Task> parceMessage(long idChat, String message) {
        List<Task> listTask = new ArrayList<>();
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(message);
        Task task;
        /*
        More than one correct task
        */
        while (matcher.find()) {
            String dateString = matcher.group(1).trim();
            String notification = matcher.group(3).trim();
            LocalDateTime date = null;
            try {
                date = LocalDateTime
                        .parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            } catch (DateTimeParseException ex) {
                LOGGER.warn("Date incorrect {}", ex);
                continue;
            }
            task = new Task(idChat, notification, date);
            listTask.add(task);
        }
        return listTask;
    }

    private boolean sendMessage(Long idChat, String message) {
        if (message == null) {
            LOGGER.warn("message is null");
            return false;
        }
        SendMessage sendMessage = new SendMessage(idChat, message);
        return telegramBot.execute(sendMessage).isOk();
    }

    @Scheduled(cron = "0 0/1 * * * *")
    private void sendMessageScheduled() {
        LOGGER.info("Scheduling a list");
        List<Task> listTask = service.getListTaskCurrentDate();
        if (!listTask.isEmpty()) {
            listTask.forEach(task -> sendMessage(task.getIdChat(), task.getNotification()));
        }
    }
}
