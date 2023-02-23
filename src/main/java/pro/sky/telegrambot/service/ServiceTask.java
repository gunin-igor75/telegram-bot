package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.Task;

import java.util.List;

public interface ServiceTask {
    Task addTask(Task task);

    List<Task> getListTaskCurrentDate();
}
