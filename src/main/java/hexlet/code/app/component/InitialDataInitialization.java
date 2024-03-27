package hexlet.code.app.component;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class InitialDataInitialization implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final String defaultEmail = "hexlet@example.com";

    private final String defaultPass = "qwerty";

    private final List<String> defaultTaskStatuses = List.of(
            "draft", "to_review", "to_be_fixed", "to_publish", "published");

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(defaultEmail).orElseGet(() -> {
            var user = new User()
                    .setEmail(defaultEmail)
                    .setPasswordDigest(passwordEncoder.encode(defaultPass));
            return userRepository.save(user);
        });

        defaultTaskStatuses.stream().map(s -> taskStatusRepository.findBySlug(s)
                .orElse(new TaskStatus()
                        .setName(s.substring(0, 1).toUpperCase() + s.substring(1))
                        .setSlug(s)
                )).forEach(taskStatusRepository::save);

    }

}
