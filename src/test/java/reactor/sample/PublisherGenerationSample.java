package reactor.sample;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

public class PublisherGenerationSample {

    private AtomicBoolean isFinished = new AtomicBoolean(false);

    @BeforeEach
    public void beforeEach(){
        isFinished.set(false);
        Awaitility.setDefaultTimeout(60, TimeUnit.SECONDS);
    }

    @Test
    public void makeFluxFromVariables(){
        Flux<String> flux = Flux.just("A", "B", "C");
        flux
                .doOnComplete(this::notifyComplete)
                .subscribe(v -> System.out.println(v));

        await().untilTrue(isFinished);
    }

    @Test
    public void makeFluxFromArray(){
        String [] array = {"AA", "BB", "CC"};
        Flux<String> flux = Flux.fromArray(array);
        flux
                .doOnComplete(this::notifyComplete)
                .subscribe(v -> System.out.println(v));

        await().untilTrue(isFinished);
    }

    @Test
    public void makeFluxFromStream(){
        List<String> list = Arrays.asList("AA", "BB", "CC");
        Flux<String> flux = Flux.fromStream(list.stream());
        flux
                .doOnComplete(this::notifyComplete)
                .subscribe(v -> System.out.println(v));

        await().untilTrue(isFinished);
    }

    @Test
    public void makeFluxFromOtherProgram() throws Exception{
        Flux<Long> flux = Flux.create(emitter -> {
            for(int i = 0; i < 100; i++){
                emitter.next(new Long(i));
            }
        });
        flux
                .doOnComplete(this::notifyComplete)
                .delayElements(Duration.ofSeconds(1))
                .subscribe(v -> System.out.println(v))
                ;

        await().untilTrue(isFinished);
    }

    @Test
    public void makeMonoFromOtherProgram() throws Exception{
        Callable<Long> callable = new Callable<>(){
            @Override
            public Long call() throws Exception{
                Thread.sleep(1000L);
                return 1L;
            }
        };

        Mono<Long> mono = Mono.fromCallable(callable);
        mono
                .doFinally(signal -> notifyComplete(signal))
                .subscribe(v -> System.out.println(v))
        ;

        await().untilTrue(isFinished);
    }

    private void notifyComplete(){
        isFinished.set(true);
    }

    private void notifyComplete(SignalType signal){
        isFinished.set(true);
    }
}
