package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InitialDataInitialization implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final String emailService = "hexlet@example.com";
    private final String passService = "qwerty";

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(emailService).orElseGet(() -> {
            var user = new User();
            user.setEmail(emailService);
            user.setPasswordDigest(passwordEncoder.encode(passService));
            return userRepository.save(user);
        });

    }

}
