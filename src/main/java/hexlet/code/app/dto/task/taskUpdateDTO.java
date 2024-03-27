package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class taskUpdateDTO {

    @NotBlank
    private JsonNullable<String> name;
    private JsonNullable<Integer> index;
    private JsonNullable<String> description;
    @NotNull
    private JsonNullable<String> taskStatus;
    private JsonNullable<Long> assigneeId;

}
