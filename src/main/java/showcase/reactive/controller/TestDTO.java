package showcase.reactive.controller;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class TestDTO {

    @Id
    private final Long id;
    private final String description;
}
