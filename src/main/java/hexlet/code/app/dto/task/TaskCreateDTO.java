package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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

}
