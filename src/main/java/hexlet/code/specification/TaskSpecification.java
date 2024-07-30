package hexlet.code.specification;

import hexlet.code.dto.task.TaskFilterDTO;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {

    public Specification<Task> build(final TaskFilterDTO filterDTO) {
        return withAssignee(filterDTO.getAssigneeId())
                .and(withTitleCont(filterDTO.getTitleCont()))
                .and(withStatus(filterDTO.getStatus()))
                .and(withLabel(filterDTO.getLabelId()));

    }

    public Specification<Task> withAssignee(final Long assigneeId) {
        return (root, query, criteriaBuilder) ->
                assigneeId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.join("assignee").get("id"), assigneeId);
    }

    public Specification<Task> withTitleCont(final String titleCont) {
        return (root, query, criteriaBuilder) ->
                titleCont == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%");
    }

    public Specification<Task> withStatus(final String status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.join("taskStatus", JoinType.INNER).get("slug"), status);
    }

    public Specification<Task> withLabel(final Long labelId) {
        return (root, query, criteriaBuilder) ->
                labelId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.join("labels", JoinType.INNER).get("id"), labelId);
    }

}
