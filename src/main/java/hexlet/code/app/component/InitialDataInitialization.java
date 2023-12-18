package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InitialDataInitialization implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        var user = new User();
        user.setEmail("hexlet@example.com");
        user.setPassword("qwerty");
        userRepository.save(user);

    }

}
