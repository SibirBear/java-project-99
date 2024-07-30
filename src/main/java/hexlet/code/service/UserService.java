package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceHasRelatedEntitiesException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceHasRelatedEntitiesException(
                    "{\"error\":\"User with id: " + id + " can`t be deleted, it has tasks\"}");
        }

    }

}
