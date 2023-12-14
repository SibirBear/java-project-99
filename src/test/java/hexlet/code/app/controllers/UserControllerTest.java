package hexlet.code.app.controllers;

import hexlet.code.app.models.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private Faker faker;

    private final String baseUrl = "api/user/";

    /**
     * Creates a {@link User} with randomly filled in parameters.
     * <p>
     * @return new {@link User}
     */
    private User generateUser() {
        return Instancio.of(User.class)
                       .ignore(Select.field(User::getId))
                       .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                       .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                       .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                       .supply(Select.field(User::getPassword), () -> faker.internet().password(3, 20))
                       .create();
    }

    @Test
    public void testGetUser() throws Exception {
        var newUser = generateUser();
        userRepository.save(newUser);

        var request = get(baseUrl + "{id}", newUser.getId());
        var result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstname").isEqualTo(newUser.getFirstName()),
                v -> v.node("lastname").isEqualTo(newUser.getLastName()),
                v -> v.node("email").isEqualTo(newUser.getEmail())
        );

    }

    @Test
    public void tesGetUserNotFound() throws Exception {
        long id = 9999;
        var request = get(baseUrl + "{id}", id);
        mockMvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    public void testListUser() throws Exception {

    }

}