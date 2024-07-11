package hexlet.code.app.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${base-url}${task-statuses-url}")
@AllArgsConstructor
public class TaskStatusController {

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TaskStatusDTO> list() {
        var taskStatuses = taskStatusRepository.findAll();

        return taskStatuses.stream().map(taskStatusMapper::map).toList();

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO getTaskStatus(@PathVariable final long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));

        return taskStatusMapper.map(taskStatus);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO createTaskStatus(@Valid @RequestBody final TaskStatusCreateDTO taskStatusBody) {
        var taskStatus = taskStatusMapper.map(taskStatusBody);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO updateTaskStatus(@Valid @RequestBody final TaskStatusUpdateDTO taskStatusBody,
                                          @PathVariable final long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        taskStatusMapper.update(taskStatusBody, taskStatus);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void deleteTaskStatus(@PathVariable final long id) {
        taskStatusRepository.deleteById(id);
    }


}
