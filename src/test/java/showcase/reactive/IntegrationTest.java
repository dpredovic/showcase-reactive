package showcase.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import showcase.reactive.controller.TestDTO;

import java.util.List;
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

        WebTestClient client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        List<TestDTO> result = client.post()
                                     .uri("/test")
                                     .accept(MediaType.TEXT_EVENT_STREAM)
                                     .body(ids, Long.class)
                                     .exchange()
                                     .expectStatus()
                                     .isOk()
                                     .expectHeader()
                                     .contentType(MediaType.TEXT_EVENT_STREAM)
                                     .expectBodyList(TestDTO.class)
                                     .returnResult()
                                     .getResponseBody();

        Set<Long> expectedIds = LongStream.range(0, count).boxed().collect(Collectors.toSet());
        for (TestDTO dto : result) {
            assertThat(dto.getDescription()).isEqualTo("Test-" + dto.getId());
            expectedIds.remove(dto.getId());
        }
        assertThat(expectedIds).isEmpty();
    }

    @Test
    public void testId() {
        long id = 123L;

        Mono<TestDTO> dtoMono = webClient.get()
                                         .uri("/test/{id}", id)
                                         .exchange()
                                         .flatMap(clientResponse -> clientResponse.bodyToMono(TestDTO.class));

        StepVerifier.create(dtoMono).consumeNextWith(dto -> {
            assertThat(dto.getId()).isEqualTo(123L);
            assertThat(dto.getDescription()).isEqualTo("Test-123");
        }).verifyComplete();
    }

    @PostConstruct
    private void init() {
        webClient = WebClient.create("http://localhost:" + port);
    }
}
