package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {

    @NotBlank
    private String name;
    private Integer index;
    private String description;
    @NotNull
    private String taskStatus;
    private Long assigneeId;
    private Set<Long> labelsIds = new HashSet<>();

}
