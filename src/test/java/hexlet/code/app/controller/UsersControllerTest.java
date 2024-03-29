package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import net.datafaker.Faker;
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

    @Value("${base-url}${users-url}")
    private String baseUrl;

    @Test
    public void testListUserWithAuth() throws Exception {
        var newUser1 = Instancio.of(modelGenerator.getUserModel()).create();
        var newUser2 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser1);
        userRepository.save(newUser2);

        var request = MockMvcRequestBuilders.get(baseUrl).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();

    }

    @Test
    public void testListUserWithoutAuth() throws Exception {
        var newUser1 = Instancio.of(modelGenerator.getUserModel()).create();
        var newUser2 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser1);
        userRepository.save(newUser2);

        var request = MockMvcRequestBuilders.get(baseUrl);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testGetUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = MockMvcRequestBuilders.get(baseUrl + "/" + newUser.getId()).with(jwt());
        var result = mockMvc.perform(request)
                             .andExpect(status().isOk())
                             .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(newUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(newUser.getLastName()),
                v -> v.node("email").isEqualTo(newUser.getEmail())
        );

    }

    @Test
    public void tesGetUserNotFound() throws Exception {
        long id = 9999;
        var request = MockMvcRequestBuilders.get(baseUrl + "/" + id).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void testCreateUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();

        var request = MockMvcRequestBuilders.post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser.getEmail()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(user.getPasswordDigest()).isNotEqualTo(newUser.getPasswordDigest());

    }

    @Test
    public void testCreateUserWithoutOptionalParams() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                                .ignore(Select.field(User::getFirstName))
                                .ignore(Select.field(User::getLastName))
                                .lenient()
                                .create();

        var request = MockMvcRequestBuilders.post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(newUser.getEmail()).orElse(null);

        assertNotNull(user);
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertThat(user.getPasswordDigest()).isNotEqualTo(newUser.getPasswordDigest());

    }

    @Test
    public void testCreateUserWithInvalidPassword() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                              .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(1, 2))
                              .create();

        var request = MockMvcRequestBuilders.post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testCreateUserWithInvalidEmail() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel())
                              .supply(Select.field(User::getEmail), () -> faker.name().fullName())
                              .create();

        var request = MockMvcRequestBuilders.post(baseUrl)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUser));
        mockMvc.perform(request).andExpect(status().isBadRequest());

    }

    @Test
    public void testUpdateUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var newUserUpdate = Instancio.of(modelGenerator.getUserModel()).create();

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser.getId()).with(jwt())
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUserUpdate.getFirstName());
        assertThat(user.getLastName()).isEqualTo(newUserUpdate.getLastName());
        assertThat(user.getEmail()).isEqualTo(newUserUpdate.getEmail());
        assertThat(user.getPasswordDigest()).isEqualTo(newUserUpdate.getPasswordDigest());
    }

    @Test
    public void testUpdateUserWithoutAuth() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var newUserUpdate = Instancio.of(modelGenerator.getUserModel()).create();

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

    @Test
    public void testUpdateUserPartial() throws Exception {
        var fnParams = "firstName";
        var lnParams = "lastName";

        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var newUserUpdate = Map.of(
                fnParams, faker.name().firstName(),
                lnParams, faker.name().lastName()
        );

        var request = MockMvcRequestBuilders.put(baseUrl + "/" + newUser.getId()).with(jwt())
                              .contentType(MediaType.APPLICATION_JSON)
                              .content(om.writeValueAsString(newUserUpdate));
        mockMvc.perform(request).andExpect(status().isOk());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(newUserUpdate.get(fnParams));
        assertThat(user.getLastName()).isEqualTo(newUserUpdate.get(lnParams));
        assertThat(user.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(user.getPasswordDigest()).isEqualTo(newUser.getPasswordDigest());
    }

    @Test
    public void testDeleteUser() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newUser.getId())
                .with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        var user = userRepository.findById(newUser.getId()).orElse(null);

        assertNull(user);

    }

    @Test
    public void testDeleteUserWithoutAuth() throws Exception {
        var newUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(newUser);

        var request = MockMvcRequestBuilders
                .delete(baseUrl + "/" + newUser.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

    }

}
