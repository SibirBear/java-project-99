package hexlet.code.app.service;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LabelService {

    @Autowired
    private final LabelRepository labelRepository;

    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final LabelMapper labelMapper;

    public List<LabelDTO> getAllLabels() {
        var labels = labelRepository.findAll();

        return labels.stream().map(labelMapper::map).toList();

    }

    public LabelDTO getLabel(final long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));

        return labelMapper.map(label);

    }

    public LabelDTO createLabel(final LabelCreateDTO labelBody) {
        var label = labelMapper.map(labelBody);
        labelRepository.save(label);

        return labelMapper.map(label);

    }

    public LabelDTO updateLabel(final long id,
                                final LabelUpdateDTO labelBody) {
        var label = labelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Label with id %s not found", id))
        );
        labelMapper.update(labelBody, label);
        labelRepository.save(label);

        return labelMapper.map(label);

    }

    public void deleteLabel(final long id) {
        //var tasks = taskRepository.findByLabelsId(id);
        /*if (!tasks.isEmpty()) {
            throw new RuntimeException(
                    String.format("Label with id %s can`t be deleted, it has tasks", id));
        }*/

        labelRepository.deleteById(id);
    }

}
