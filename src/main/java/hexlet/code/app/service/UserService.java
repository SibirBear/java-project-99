package hexlet.code.app.service;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private TaskRepository taskRepository;

    public List<UserDTO> getAllUsers() {
        var users = userRepository.findAll();

        return users.stream().map(userMapper::map).toList();

    }

    public UserDTO getUserById(final long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));

        return userMapper.map(user);

    }

    public UserDTO createUser(final UserCreateDTO userCreateDTO) {
        var user = userMapper.map(userCreateDTO);
        userRepository.save(user);

        return userMapper.map(user);

    }

    public UserDTO updateUser(final UserUpdateDTO userUpdateDTO, final long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));
        userMapper.update(userUpdateDTO, user);
        userRepository.save(user);

        return userMapper.map(user);

    }

    public void deleteUser(final long id) {
        var tasks = taskRepository.findByAssigneeId(id);
        if (!tasks.isEmpty()) {
            throw new RuntimeException(
                    String.format("User with id %s can`t be deleted, it has tasks", id));
        }

        userRepository.deleteById(id);

    }

}
