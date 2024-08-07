package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.stream.Collectors;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskMapper taskMapper;

    private String baseUrl = "/api/tasks";

    private String userEmail = "hexlet@example.com";

    private List<String> taskStatuses = List.of(
            "draft", "to_review", "to_be_fixed", "to_publish", "published");;

    private List<String> defaultLabels = List.of("bug", "feature");;

    private Task testTask;

    @BeforeEach
    public void setUp() {
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();

        testTask.setTaskStatus(taskStatusRepository.findBySlug(taskStatuses.get(0))
                .orElseThrow(() -> new RuntimeException("TaskStatus not found.")));
        testTask.setAssignee(userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found.")));
        testTask.setLabels(labelRepository.findByName(defaultLabels.get(0))
                .stream().collect(Collectors.toSet()));

        taskRepository.save(testTask);

    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteById(testTask.getId());
    }

    @Test
    public void testGetTask() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + testTask.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId())
        );

    }

    @Test
    public void testGetTaskNotFound() throws Exception {
        mockMvc.perform(delete(baseUrl + "/" + testTask.getId()).with(jwt()));
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + testTask.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void testGetTaskWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testListTasks() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

        var listTasks = taskRepository.findAll();

        for (var task : listTasks) {
            assertThat(body).contains(String.valueOf(task.getId()));
            assertThat(body).contains(task.getName());
            assertThat(body).contains(task.getDescription());
        }

    }

    @Test
    public void testListTasksWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testTaskCreate() throws Exception {
        cleanUp();

        var taskCreateDTO = taskMapper.map(testTask);

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskCreateDTO));
        mockMvc.perform(request).andExpect(status().isCreated());

        var task = taskRepository.findByName(taskCreateDTO.getTitle()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskCreateDTO.getTitle());
        assertThat(task.getIndex()).isEqualTo(taskCreateDTO.getIndex());
        assertThat(task.getDescription()).isEqualTo(taskCreateDTO.getContent());
        assertThat(task.getAssignee().getId()).isEqualTo(taskCreateDTO.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(taskCreateDTO.getStatus());

    }

    @Test
    public void testCreateTaskWithoutIndexAndDescription() throws Exception {
        var testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .ignore(Select.field(Task::getDescription))
                .ignore(Select.field(Task::getIndex))
                .lenient()
                .create();
        testTask2.setTaskStatus(taskStatusRepository.findBySlug(taskStatuses.get(0)).orElse(null));
        testTask2.setAssignee(userRepository.findByEmail(userEmail).orElse(null));

        var taskCreateDto = taskMapper.map(testTask2);

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskCreateDto));
        mockMvc.perform(request).andExpect(status().isCreated());

        var task = taskRepository.findByName(testTask2.getName()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskCreateDto.getTitle());
        assertThat(task.getAssignee().getId()).isEqualTo(taskCreateDto.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(taskCreateDto.getStatus());
        assertThat(task.getDescription()).isNullOrEmpty();

    }

    @Test
    public void testCreateTaskWithoutName() throws Exception {
        var testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .ignore(Select.field(Task::getName))
                .lenient()
                .create();

        var dto = taskMapper.map(testTask2);

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateTaskWithoutTaskStatus() throws Exception {
        var testTask2 = Instancio.of(modelGenerator.getTaskModel())
                .ignore(Select.field(Task::getTaskStatus))
                .lenient()
                .create();

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTask2));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateTaskWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testTask));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateTask() throws Exception {
        var newTaskUpdate = Instancio.of(modelGenerator.getTaskModel()).create();
        newTaskUpdate.setTaskStatus(taskStatusRepository.findBySlug(taskStatuses.get(3))
                .orElseThrow(() -> new RuntimeException("TaskStatus not found.")));
        newTaskUpdate.setAssignee(userRepository.save(Instancio.of(modelGenerator.getUserModel()).create()));

        var dto = taskMapper.map(newTaskUpdate);

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(dto.getTitle());
        assertThat(task.getIndex()).isEqualTo(dto.getIndex());
        assertThat(task.getDescription()).isEqualTo(dto.getContent());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getStatus());
        assertThat(task.getAssignee().getId()).isEqualTo(dto.getAssigneeId());

    }

    @Test
    public void testUpdateTaskWithoutAuth() throws Exception {
        var newTaskUpdate = Instancio.of(modelGenerator.getTaskModel()).create();
        var dto = taskMapper.map(newTaskUpdate);

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateTaskPartialWithEmptyName() throws Exception {

        var dto = new TaskUpdateDTO();
        dto.setTitle(JsonNullable.of("   "));

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isBadRequest());

        var task = taskRepository.findById(testTask.getId()).orElse(null);

        assertNotNull(task);
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTask.getTaskStatus().getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(testTask.getAssignee().getId());

    }

    @Test
    public void testUpdateTaskPartialWithWrongTaskStatus() throws Exception {

        var dto = new TaskUpdateDTO();
        dto.setStatus(JsonNullable.of("something-wrong"));

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    public void testDeleteTask() throws Exception {
        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + testTask.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        var task = taskStatusRepository.findById(testTask.getId()).orElse(null);

        assertNull(task);

    }

    @Test
    public void testDeleteTaskStatusWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + testTask.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

}
