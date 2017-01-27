package showcase.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import showcase.reactive.controller.TestDTO;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    private static final WebClient webClient = WebClient.create(new ReactorClientHttpConnector());

    @LocalServerPort
    private int port;

    @Test
    public void testIds() {
        int count = 43;
        Publisher<Long> ids = Flux.fromStream(LongStream.range(0, count).boxed()).log();

        ClientRequest<Publisher<Long>> req =
            ClientRequest.POST("http://localhost:{port}/test", port).body(ids, Long.class);

        Flux<TestDTO> dtoFlux = webClient.exchange(req).flatMap(resp -> resp.bodyToFlux(TestDTO.class));

        Set<Long> expectedIds = LongStream.range(0, count).boxed().collect(Collectors.toSet());

        StepVerifier.create(dtoFlux).thenConsumeWhile(dto -> !expectedIds.isEmpty(), dto -> {
            assertThat(dto.getDescription()).isEqualTo("Test-" + dto.getId());
            expectedIds.remove(dto.getId());
        }).expectComplete().verify(Duration.ofSeconds(60));
    }

    @Test
    public void testId() {
        long id = 1234L;

        ClientRequest<Void> req = ClientRequest.GET("http://localhost:{port}/test/{id}", port, id).build();

        Mono<TestDTO> dtoMono = webClient.exchange(req).then(resp -> resp.bodyToMono(TestDTO.class));

        StepVerifier.create(dtoMono).consumeNextWith(dto -> {
            assertThat(dto.getId()).isEqualTo(1234L);
            assertThat(dto.getDescription()).isEqualTo("Test-1234");
        }).expectComplete().verify();
    }
}
