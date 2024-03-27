package hexlet.code.app.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class taskDTO {

    private Long id;
    private String name;
    private Integer index;
    private String description;
    private String taskStatus;
    private Long assigneeId;
    private LocalDate createdAt;

}
