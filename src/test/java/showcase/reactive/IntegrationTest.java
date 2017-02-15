package showcase.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import showcase.reactive.controller.TestDTO;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @Test
    public void testIds() {
        int count = 1000;
        Publisher<Long> ids = Flux.fromStream(LongStream.range(0, count).boxed());

        Flux<TestDTO> dtoFlux =
            webClient.post().uri("/test").exchange(ids, Long.class).flatMap(resp -> resp.bodyToFlux(TestDTO.class));

        Set<Long> expectedIds = LongStream.range(0, count).boxed().collect(Collectors.toSet());

        StepVerifier.create(dtoFlux).thenConsumeWhile(dto -> !expectedIds.isEmpty(), dto -> {
            assertThat(dto.getDescription()).isEqualTo("Test-" + dto.getId());
            expectedIds.remove(dto.getId());
        }).expectComplete().verify(Duration.ofSeconds(60));
    }

    @Test
    public void testId() {
        long id = 1234L;

        Mono<TestDTO> dtoMono =
            webClient.get().uri("/test/{id}", id).exchange().then(resp -> resp.bodyToMono(TestDTO.class));

        StepVerifier.create(dtoMono).consumeNextWith(dto -> {
            assertThat(dto.getId()).isEqualTo(1234L);
            assertThat(dto.getDescription()).isEqualTo("Test-1234");
        }).verifyComplete();
    }

    @PostConstruct
    private void init() {
        webClient = WebClient.create("http://localhost:" + port);
    }
}
