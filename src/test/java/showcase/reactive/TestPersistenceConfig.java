package showcase.reactive;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import showcase.reactive.controller.TestDTO;
import showcase.reactive.persistence.TestReactiveRepository;

import java.util.stream.LongStream;

@Configuration
public class TestPersistenceConfig {

    @Bean
    CommandLineRunner setupMongodb(TestReactiveRepository repository) {
        return (p) -> {
            int size = 1000;
            Long count = repository.count().block();
            if (count != size) {
                repository.deleteAll().block();
                repository.saveAll(
                    Flux.fromStream(LongStream.range(0, size).boxed()).map(id -> new TestDTO(id, "Test-" + id)))
                          .blockLast();
            }
        };
    }
}
