package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pro.sky.telegrambot.model.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "select * from task where task.date = ?1 ", nativeQuery = true)
    List<Task> getListTaskCurrentDate(LocalDateTime currentDate);
}
