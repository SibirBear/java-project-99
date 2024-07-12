package hexlet.code.app.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {

    private Long id;
    private String name;
    private Integer index;
    private String description;
    private String taskStatus;
    private Long assigneeId;
    private Set<Long> labelsIds = new HashSet<>();
    private LocalDate createdAt;

}
