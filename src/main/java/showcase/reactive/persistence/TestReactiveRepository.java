package showcase.reactive.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import showcase.reactive.controller.TestDTO;

public interface TestReactiveRepository extends ReactiveCrudRepository<TestDTO, Long> {

}
