package hexlet.code.app.util;

import hexlet.code.app.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ModelGenerator {

	@Autowired
	private Faker faker;

	private Model<User> userModel;

	@PostConstruct
	private void init() {
		userModel = Instancio.of(User.class)
							.ignore(Select.field(User::getId))
							.supply(Select.field(User::getFirstName),
									() -> faker.name().firstName())
							.supply(Select.field(User::getLastName),
									() -> faker.name().lastName())
							.supply(Select.field(User::getEmail),
									() -> faker.internet().emailAddress())
							.supply(Select.field(User::getPasswordDigest),
									() -> faker.internet().password(3, 20))
							.toModel();

	}

}
