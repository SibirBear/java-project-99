package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    @Value("${base-url}${task-statuses-url}")
    private String baseUrl;

    @Test
    public void testGetTaskStatus() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var request = MockMvcRequestBuilders.get(baseUrl + "/" + newTaskStatus.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(newTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(newTaskStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(newTaskStatus.getCreatedAt())
        );

    }

    @Test
    public void testGetTaskStatusNotFound() throws Exception {
        long id = Long.MAX_VALUE;
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + id).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void testListTaskStatuses() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var request = MockMvcRequestBuilders.get(baseUrl).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

    }

    @Test
    public void testTaskStatusCreate() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatus));
        mockMvc.perform(request).andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(newTaskStatus.getSlug()).orElse(null);

        assertNotNull(taskStatus);
        assertThat(taskStatus.getName()).isEqualTo(newTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(newTaskStatus.getSlug());

    }

    @Test
    public void testCreateTaskStatusWithoutName() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel())
                .ignore(Select.field(TaskStatus::getName))
                .lenient()
                .create();

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatus));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateTaskStatusWithoutSlug() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel())
                .ignore(Select.field(TaskStatus::getSlug))
                .lenient()
                .create();

        var request = MockMvcRequestBuilders.post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatus));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateTaskStatusWithoutAuth() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = MockMvcRequestBuilders.post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatus));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var newTaskStatusUpdate = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatusUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(newTaskStatus.getId()).orElse(null);

        assertNotNull(taskStatus);
        assertThat(taskStatus.getName()).isEqualTo(newTaskStatusUpdate.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(newTaskStatusUpdate.getSlug());

    }

    @Test
    public void testUpdateTaskStatusWithoutAuth() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var newTaskStatusUpdate = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatusUpdate));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateTaskStatusPartial() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var newTaskStatusUpdate = Instancio.of(modelGenerator.getTaskStatusModel())
                .ignore(Select.field(TaskStatus::getName))
                .lenient()
                .create();

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskStatusUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(newTaskStatus.getId()).orElse(null);

        assertNotNull(taskStatus);

        assertThat(taskStatus.getName()).isEqualTo(newTaskStatus.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(newTaskStatusUpdate.getSlug());

    }
    //delete
    @Test
    public void testDeleteTaskStatus() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newTaskStatus.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNotFound());

        var taskStatus = taskStatusRepository.findById(newTaskStatus.getId()).orElse(null);

        assertNull(taskStatus);

    }

    @Test
    public void testDeleteTaskStatusWithoutAuth() throws Exception {
        var newTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(newTaskStatus);

        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newTaskStatus.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

}
