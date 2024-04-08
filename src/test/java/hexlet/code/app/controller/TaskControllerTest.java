package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

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
    private TaskMapper taskMapper;

    @Value("${base-url}${task-url}")
    private String baseUrl;

    @Value("${const-user-email}")
    private String userEmail;

    @Value("#{'${const-task-status-slugs}'.split(',')}")
    private List<String> taskStatuses;

    private Task testTask;

    @BeforeEach
    public void setUp() {
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();

        testTask.setTaskStatus(taskStatusRepository.findBySlug(taskStatuses.get(0))
                .orElseThrow(() -> new RuntimeException("TaskStatus not found.")));
        testTask.setAssignee(userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found.")));

        taskRepository.save(testTask);

    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteById(testTask.getId());
    }

    /*private TaskCreateDTO getCreateTaskDto(Task task) {
        TaskCreateDTO dto = new TaskCreateDTO();

        dto.setName(task.getName());
        dto.setIndex(task.getIndex());
        dto.setDescription(task.getDescription());
        dto.setAssigneeId(task.getAssignee().getId());
        dto.setTaskStatus(task.getTaskStatus().getSlug());

        return dto;

    }

    private TaskUpdateDTO getUpdateTaskDto(Task task) {
        TaskUpdateDTO dto = new TaskUpdateDTO();

        dto.setName(JsonNullable.of(task.getName()));
        dto.setIndex(JsonNullable.of(task.getIndex()));
        dto.setDescription(JsonNullable.of(task.getDescription()));
        dto.setAssigneeId(JsonNullable.of(task.getAssignee().getId()));
        dto.setTaskStatus(JsonNullable.of(task.getTaskStatus().getSlug()));

        return dto;

    }*/

    @Test
    public void testGetTask() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + testTask.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("name").isEqualTo(testTask.getName()),
                v -> v.node("description").isEqualTo(testTask.getDescription()),
                v -> v.node("taskStatus").isEqualTo(testTask.getTaskStatus().getSlug()),
                v -> v.node("assigneeId").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("createdAt").isEqualTo(testTask.getCreatedAt())
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

        var task = taskRepository.findByName(taskCreateDTO.getName()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskCreateDTO.getName());
        assertThat(task.getIndex()).isEqualTo(taskCreateDTO.getIndex());
        assertThat(task.getDescription()).isEqualTo(taskCreateDTO.getDescription());
        assertThat(task.getAssignee().getId()).isEqualTo(taskCreateDTO.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(taskCreateDTO.getTaskStatus());

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
        assertThat(task.getName()).isEqualTo(taskCreateDto.getName());
        assertThat(task.getAssignee().getId()).isEqualTo(taskCreateDto.getAssigneeId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(taskCreateDto.getTaskStatus());
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
        assertThat(task.getName()).isEqualTo(dto.getName());
        assertThat(task.getIndex()).isEqualTo(dto.getIndex());
        assertThat(task.getDescription()).isEqualTo(dto.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(dto.getTaskStatus());
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
        dto.setName(JsonNullable.of("   "));

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + testTask.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));
        mockMvc.perform(request).andExpect(status().isBadRequest());

        var task = taskRepository.findById(testTask.getId()).get();

        assertNotNull(task);
        assertThat(task.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTask.getTaskStatus().getSlug());
        assertThat(task.getAssignee().getId()).isEqualTo(testTask.getAssignee().getId());

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
