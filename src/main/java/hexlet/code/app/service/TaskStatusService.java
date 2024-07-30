package hexlet.code.app.service;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceHasRelatedEntitiesException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatusService {

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAllTaskStatuses() {
        var taskStatuses = taskStatusRepository.findAll();

        return taskStatuses.stream()
                .map(taskStatusMapper::map).toList();

    }

    public TaskStatusDTO getTaskStatus(final long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));

        return taskStatusMapper.map(taskStatus);

    }

    public TaskStatusDTO createTaskStatus(final TaskStatusCreateDTO taskStatusBody) {
        var taskStatus = taskStatusMapper.map(taskStatusBody);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);

    }

    public TaskStatusDTO updateTaskStatus(final TaskStatusUpdateDTO taskStatusBody,
                                          final long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        taskStatusMapper.update(taskStatusBody, taskStatus);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);

    }

    public void deleteTaskStatus(final long id) {
        try {
            taskStatusRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceHasRelatedEntitiesException(
                    "{\"error\":\"Task status with id: " + id + " can`t be deleted, it has tasks\"}");
        }
    }

}
