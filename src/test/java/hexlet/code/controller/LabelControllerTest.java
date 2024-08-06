package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    private User testUser;

    private JwtRequestPostProcessor token;

    private Label testLabel;
    private Label newLabel;

    @Value("${base-url}${label-url}")
    private String baseUrl;

    @BeforeEach
    void setUp() {
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        newLabel = Instancio.of(modelGenerator.getLabelModel()).create();

    }

    @AfterEach
    void tearDown() {
        userRepository.delete(testUser);
        labelRepository.delete(testLabel);
        labelRepository.delete(newLabel);

    }

    @Test
    @Order(1)
    public void testGetLabel() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl + "/{id}", testLabel.getId())
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );

    }

    @Test
    @Order(2)
    public void testGetAllLabels() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl)
                .with(token);
        var result = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

    }

    @Test
    @Order(3)
    public void testCreateLabelPositive() throws Exception {
        var request = MockMvcRequestBuilders.post(baseUrl)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelMapper.map(newLabel)));
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var label = labelRepository.findByName(newLabel.getName()).orElseThrow();

        assertThat(label.getName()).isNotNull();
        assertThat(label.getName()).isEqualTo(newLabel.getName());

    }

    @Test
    @Order(4)
    public void testCreateLabelNegativeNotValidName() throws Exception {
        newLabel.setName("");

        var request = MockMvcRequestBuilders.post(baseUrl)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelMapper.map(newLabel)));
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    @Order(5)
    public void testUpdateLabelPositive() throws Exception {
        var request = MockMvcRequestBuilders.put(baseUrl + "/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelMapper.map(newLabel)));
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());

        var label = labelRepository.findById(testLabel.getId()).orElse(null);

        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(newLabel.getName());
        assertThat(label.getTasks().size()).isEqualTo(testLabel.getTasks().size());

    }

    @Test
    @Order(6)
    public void testUpdateLabelNegativeWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.put(baseUrl + "/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelMapper.map(newLabel)));
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    @Order(7)
    public void testDeleteLabelPositive() throws Exception {
        var request = MockMvcRequestBuilders.delete(baseUrl + "/{id}", testLabel.getId())
                .with(token);
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        var label = labelRepository.findById(testLabel.getId()).orElse(null);

        assertNull(label);

    }

    @Test
    @Order(8)
    public void testDeleteLabelNegativeWrongId() throws Exception {
        var request = MockMvcRequestBuilders.delete(baseUrl + "/{id}", Long.MAX_VALUE)
                .with(token);
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

}
