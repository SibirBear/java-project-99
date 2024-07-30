package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceHasRelatedEntitiesException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            labelRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceHasRelatedEntitiesException(
                    "{\"error\":\"Label with id: " + id + " can`t be deleted, it has tasks\"}");
        }

    }

}
