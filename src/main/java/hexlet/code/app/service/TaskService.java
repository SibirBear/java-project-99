package hexlet.code.app.service;

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
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskService {

    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final TaskMapper taskMapper;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final LabelRepository labelRepository;

    @Transactional
    public List<TaskDTO> getAllTasks() {
        var tasks = taskRepository.findAll();

        return tasks.stream().map(taskMapper::map).toList();

    }

    @Transactional
    public TaskDTO getTask(final Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));

        return taskMapper.map(task);

    }

    public TaskDTO createTask(final TaskCreateDTO taskCreateDTO) {
        var task = taskMapper.map(taskCreateDTO);
        var assigneeId = taskCreateDTO.getAssigneeId();

        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId).orElse(null);
            task.setAssignee(user);

        }

        var slug = taskCreateDTO.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(slug).orElse(null);
        task.setTaskStatus(taskStatus);

        var labels = task.getLabels();
        labels.forEach(label -> label.addTask(task));

        taskRepository.save(task);

        return taskMapper.map(task);

    }

    @Transactional
    public TaskDTO updateTask(final TaskUpdateDTO taskBody, final long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        taskMapper.update(taskBody, task);

        var assigneeId = taskBody.getAssigneeId();
        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId.get()).orElse(null);
            task.setAssignee(user);

        }

        var slug = taskBody.getStatus();
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

    public void deleteTask(final long id) {
        /*var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));*/

        taskRepository.deleteById(id);

    }

}
