package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
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
	private Model<TaskStatus> taskStatusModel;
	private Model<Task> taskModel;
	private Model<Label> labelModel;

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

		// иногда падали тесты, совершенно рандомно: могли упасть все, часть или вообще ни одного
		// было решено добавить случайное число к сгенерируемому значению поля
		// для этого реализован метод, который возвращает случайное число
		taskStatusModel = Instancio.of(TaskStatus.class)
							.ignore(Select.field(TaskStatus::getId))
							.supply(Select.field(TaskStatus::getName),
									() -> faker.book().author() + generateRandom())
							.supply(Select.field(TaskStatus::getSlug),
									() -> faker.lorem().word() + generateRandom())
							.toModel();

		taskModel = Instancio.of(Task.class)
							.ignore(Select.field(Task::getId))
							.supply(Select.field(Task::getName),
									() -> faker.lorem().word())
							.supply(Select.field(Task::getDescription),
									() -> faker.lorem().paragraph())
							.toModel();

		labelModel = Instancio.of(Label.class)
							.ignore(Select.field(Label::getId))
							.supply(Select.field(Label::getName),
									() -> faker.lorem().characters(3, 100))
							.ignore(Select.field(Label::getTasks))
							.toModel();

	}

	private String generateRandom() {
		return String.valueOf(Math.abs(Math.random() * 100));
	}

}
