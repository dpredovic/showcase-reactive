package showcase.reactive.controller;

import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.reactive.persistence.TestReactiveRepository;

@RestController
public class TestController {

    private final TestReactiveRepository repository;

    public TestController(TestReactiveRepository repository) {
        this.repository = repository;
    }

    @GetMapping(path = "/test/{id}")
    public Mono<TestDTO> findById(@PathVariable("id") Long id) {
        return repository.findById(id);
    }

    @PostMapping(path = "/test")
    public Flux<TestDTO> findByIds(@RequestBody Publisher<Long> ids) {
        return repository.findAllById(ids);
    }
}
