package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Task;
import pro.sky.telegrambot.repository.TaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ServiceTaskImp implements ServiceTask{
    private final TaskRepository repository;

    public ServiceTaskImp(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public Task addTask(Task task) {
        return repository.save(task);
    }

    @Override
    public List<Task> getListTaskCurrentDate() {
        LocalDateTime currentDate =LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return repository.getListTaskCurrentDate(currentDate);
    }
}
