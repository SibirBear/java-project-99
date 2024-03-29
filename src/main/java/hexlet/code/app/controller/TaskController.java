package hexlet.code.app.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${base-url}${task-url}")
@AllArgsConstructor
public class TaskController {

}
