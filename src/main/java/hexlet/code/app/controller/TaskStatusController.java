package hexlet.code.app.controller;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.service.TaskStatusService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final TaskStatusService taskStatusService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> getAll() {
        List<TaskStatusDTO> taskStatuses = taskStatusService.getAllTaskStatuses();

        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(taskStatuses.size()))
                .body(taskStatuses);

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO get(@PathVariable final long id) {
        return taskStatusService.getTaskStatus(id);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO createTaskStatus(@Valid @RequestBody final TaskStatusCreateDTO taskStatusBody) {
        return taskStatusService.createTaskStatus(taskStatusBody);

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO updateTaskStatus(@Valid @RequestBody final TaskStatusUpdateDTO taskStatusBody,
                                          @PathVariable final long id) {
        return taskStatusService.updateTaskStatus(taskStatusBody, id);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void deleteTaskStatus(@PathVariable final long id) {
        taskStatusService.deleteTaskStatus(id);
    }


}
