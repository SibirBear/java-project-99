package hexlet.code.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.models.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private final String baseUrl = "/api/users/";

    @Test
    @Transactional
    public void testListUser() throws Exception {
        var newUser1 = Instancio.of(modelGenerator.getUserModel()).create();
        var newUser2 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser1);
        userRepository.save(newUser2);

        var request = get(baseUrl);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

    }

    @Test
    @Transactional
    public void testGetUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = get(baseUrl + newUser.getId());
        var result = mockMvc.perform(request)
                             .andExpect(status().isOk())
                             .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstname").isEqualTo(newUser.getFirstName()),
                v -> v.node("lastname").isEqualTo(newUser.getLastName()),
                v -> v.node("email").isEqualTo(newUser.getEmail())
        );

    }

    @Test
    @Transactional
    public void tesGetUserNotFound() throws Exception {
        long id = 9999;
        var request = get(baseUrl + id);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @Transactional
    public void testCreateUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();

        var request = post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser.getEmail()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(user.getPassword()).isEqualTo(newUser.getPassword());

    }

    @Test
    @Transactional
    public void testCreateUserWithoutOptionalParams() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                              .ignore(Select.field(User::getFirstName))
                              .ignore(Select.field(User::getLastName))
                              .create();

        var request = post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser.getEmail()).orElse(null);

        assertNotNull(user);
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertThat(user.getPassword()).isEqualTo(newUser.getPassword());

    }

    @Test
    @Transactional
    public void testCreateUserWithInvalidPassword() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                              .supply(Select.field(User::getPassword), () -> faker.internet().password(1, 2))
                              .create();

        var request = post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    public void testCreateUserWithInvalidEmail() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                              .supply(Select.field(User::getEmail), () -> faker.name().fullName())
                              .create();

        var request = post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    public void testUpdateUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var newUserUpdate = Instancio.of(modelGenerator.getUserModel()).create();

        var request = put(baseUrl + newUser.getId())
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(user.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(user.getPassword()).isEqualTo(newUser.getPassword());
    }

    @Test
    @Transactional
    public void testUpdateUserPartial() throws Exception {
        var fnParams = "firstname";
        var lnParams = "lastname";

        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var newUserUpdate = Map.of(
                fnParams, faker.name().firstName(),
                lnParams, faker.name().lastName()
        );

        var request = put(baseUrl + newUser.getId())
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUserUpdate.get(fnParams));
        assertThat(user.getLastName()).isEqualTo(newUserUpdate.get(lnParams));
        assertThat(user.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(user.getPassword()).isEqualTo(newUser.getPassword());
    }

    @Test
    @Transactional
    public void testDeleteUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = delete(baseUrl + newUser.getId());

        mockMvc.perform(request).andExpect(status().isNoContent());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNull(user);

    }

}
