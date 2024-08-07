package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    private String baseUrl = "/api/users";

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User newUser1;
    private User newUser2;

    @BeforeEach
    void setUp() {
        newUser1 = Instancio.of(modelGenerator.getUserModel()).create();
        newUser2 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser2);
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteById(newUser2.getId());
        userRepository.deleteById(newUser1.getId());

    }

    @Test
    public void testListUserWithAuth() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

    }

    @Test
    public void testListUserWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testGetUser() throws Exception {
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + newUser2.getId())
                .with(token);
        var result = mockMvc.perform(request)
                             .andExpect(status().isOk())
                             .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(newUser2.getFirstName()),
                v -> v.node("lastName").isEqualTo(newUser2.getLastName()),
                v -> v.node("email").isEqualTo(newUser2.getEmail())
        );

    }

    @Test
    public void tesGetUserNotFound() throws Exception {
        long id = 9999;
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + id)
                .with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void testCreateUser() throws Exception {
        var request = MockMvcRequestBuilders.post(baseUrl).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser1.getEmail()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUser1.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUser1.getLastName());
        assertThat(user.getPasswordDigest()).isNotEqualTo(newUser1.getPasswordDigest());

    }

    @Test
    public void testCreateUserWithoutOptionalParams() throws Exception {
        newUser1.setFirstName(null);
        newUser1.setLastName(null);

        var request = MockMvcRequestBuilders.post(baseUrl).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser1.getEmail()).orElse(null);

        assertNotNull(user);
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertThat(user.getPasswordDigest()).isNotEqualTo(newUser1.getPasswordDigest());

    }

    @Test
    public void testCreateUserWithInvalidPassword() throws Exception {
        newUser1.setPasswordDigest("11");

        var request = MockMvcRequestBuilders.post(baseUrl).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateUserWithInvalidEmail() throws Exception {
        newUser1.setEmail("test-email");

        var request = MockMvcRequestBuilders.post(baseUrl).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testUpdateUser() throws Exception {
        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser2.getId()).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser2.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUser1.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUser1.getLastName());
        assertThat(user.getEmail()).isEqualTo(newUser1.getEmail());
        assertThat(user.getPasswordDigest()).isEqualTo(newUser1.getPasswordDigest());
    }

    @Test
    public void testUpdateUserWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newUser1));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateUserPartial() throws Exception {
        var fnParams = "firstName";
        var lnParams = "lastName";

        var newUserUpdate = Map.of(
                fnParams, faker.name().firstName(),
                lnParams, faker.name().lastName()
        );

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser2.getId()).with(token)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser2.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUserUpdate.get(fnParams));
        assertThat(user.getLastName()).isEqualTo(newUserUpdate.get(lnParams));
        assertThat(user.getEmail()).isEqualTo(newUser2.getEmail());
        assertThat(user.getPasswordDigest()).isEqualTo(newUser2.getPasswordDigest());
    }

    @Test
    public void testDeleteUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newUser.getId())
                .with(token);

        mockMvc.perform(request).andExpect(status().isNoContent());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNull(user);

    }

    @Test
    public void testDeleteUserWithoutAuth() throws Exception {
        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newUser2.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

}
