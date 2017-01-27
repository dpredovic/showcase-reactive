package showcase.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import showcase.reactive.controller.TestController;
import showcase.reactive.controller.TestDTO;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ControllerTest {

    @Autowired
    private TestController controller;

    @Test
    public void testIds() {
        int count = 1000;
        Flux<Long> ids = Flux.fromStream(LongStream.range(0, count).boxed());

        Flux<TestDTO> dtoFlux = controller.findByIds(ids);

        Set<Long> expectedIds = LongStream.range(0, count).boxed().collect(Collectors.toSet());
        StepVerifier.create(dtoFlux)
                    .thenConsumeWhile(dto -> !expectedIds.isEmpty(), dto -> expectedIds.remove(dto.getId()))
                    .expectComplete()
                    .verify();
        assertThat(expectedIds).isEmpty();
    }

    @Test
    public void testId() {
        Mono<TestDTO> dtoMono = controller.findById(1234L);

        StepVerifier.create(dtoMono).consumeNextWith(dto -> {
            assertThat(dto.getId()).isEqualTo(1234L);
            assertThat(dto.getDescription()).isEqualTo("Test-1234");
        }).expectComplete().verify();
    }
}
