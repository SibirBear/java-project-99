package hexlet.code.app.controller;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.UserUtils;
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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${base-url}${task-url}")
@AllArgsConstructor
public class TaskController {

    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final LabelRepository labelRepository;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final TaskMapper taskMapper;

    @Autowired
    private final UserUtils userUtils;

    //get all
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDTO> list() {
        var task = taskRepository.findAll();

        return task.stream().map(taskMapper::map).toList();

    }

    //get id
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO getTask(@PathVariable final long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));

        return taskMapper.map(task);

    }

    //post create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody final TaskCreateDTO taskBody) {
        var task = taskMapper.map(taskBody);
        var assigneeId = taskBody.getAssigneeId();

        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId).orElse(null);
            task.setAssignee(user);

        }
        var slug = taskBody.getTaskStatus();
        var taskStatus = taskStatusRepository.findBySlug(slug).orElse(null);
        task.setTaskStatus(taskStatus);

        var labels = task.getLabels();
        labels.forEach(label -> label.addTask(task));

        taskRepository.save(task);

        return taskMapper.map(task);

    }

    //put update
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO updateTask(@Valid @RequestBody final TaskUpdateDTO taskBody,
                                          @PathVariable final long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        taskMapper.update(taskBody, task);

        var assigneeId = taskBody.getAssigneeId();
        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId.get()).orElse(null);
            task.setAssignee(user);

        }

        var slug = taskBody.getTaskStatus();
        if (slug != null) {
            var taskStatus = taskStatusRepository.findBySlug(slug.get()).orElse(null);
            task.setTaskStatus(taskStatus);
        }

        Set<Long> labelsId = task.getLabels().stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        Set<Label> labels = labelRepository.findByIdIn(labelsId);
        labels.forEach(a -> a.addTask(task));

        taskRepository.save(task);

        return taskMapper.map(task);

    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void deleteTask(@PathVariable final long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        task.getLabels().forEach(label -> label.removeTask(task));

        taskRepository.deleteById(id);
    }

}
