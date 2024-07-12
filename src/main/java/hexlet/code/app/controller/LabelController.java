package hexlet.code.app.controller;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${base-url}${label-url}")
@AllArgsConstructor
public class LabelController {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LabelDTO> getAllLabels() {
        var labels = labelRepository.findAll();

        return labels.stream().map(labelMapper::map).toList();

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO getLabel(@PathVariable final long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));

        return labelMapper.map(label);

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody final LabelCreateDTO labelBody) {
        var label = labelMapper.map(labelBody);

        labelRepository.save(label);

        return labelMapper.map(label);

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO updateLabel(@PathVariable final long id,
                                @Valid @RequestBody final LabelUpdateDTO labelBody) {
        var label = labelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format("Label with id %s not found", id))
        );
        labelMapper.update(labelBody, label);
        labelRepository.save(label);

        return labelMapper.map(label);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void deleteLabel(@PathVariable final long id) {
        labelRepository.deleteById(id);
    }

}

