package reactor.sample;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.SignalType;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.awaitility.Awaitility.await;

public class ConversionSample {
    private AtomicBoolean isFinished = new AtomicBoolean(false);

    @BeforeEach
    public void beforeEach(){
        isFinished.set(false);
        Awaitility.setDefaultTimeout(60, TimeUnit.SECONDS);
    }

    // map
    @Test
    public void monoMapConversion(){
        Mono<ProductEntity> mono = getAProduct(1);
        mono
            .doFinally(signal -> notifyComplete(signal))
            .map(entity -> toDto(entity))
            .subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    @Test
    public void monoMapConversionWithLogging(){
        Mono<ProductEntity> mono = getAProduct(1);
        mono
            .doFinally(signal -> notifyComplete(signal))
            .doOnNext(entity -> System.out.println(entity))
            .map(entity -> toDto(entity))
            .doOnNext(dto -> System.out.println(dto))
            .subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    @Test
    public void fluxMap(){
        Flux<ProductEntity> flux = getProducts();
        flux
            .doOnComplete(this::notifyComplete)
            .map(entity -> toDto(entity))
            .subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    // Mono.map invokes sync function
    @Test
    public void monoMapSyncCall(){
        Mono<ProductEntity> mono = getAProduct(1);
        mono
            .doFinally(signal -> notifyComplete(signal))
            .map(entity -> getAReviewSync(entity.getId()))
            .subscribe(review -> System.out.println(review));

        await().untilTrue(isFinished);
    }

    // Mono.flapMap invokes async function
    @Test
    public void monoFlatMapAsyncCall(){
        Mono<ProductEntity> mono = getAProduct(1);
        mono
            .doFinally(signal -> notifyComplete(signal))
            .flatMap(entity -> getAReviewAsync(entity.getId()))
            .subscribe(review -> System.out.println(review));

        await().untilTrue(isFinished);
    }

    // Mono.flatMapMany
    @Test
    public void monoFlatMapMany(){
        Mono<ProductEntity> mono = getAProduct(1);
        mono
            .doFinally(signal -> notifyComplete(signal))
            .flatMapMany(entity -> getReviewsAsync(entity.getId()))
            .subscribe(review -> System.out.println(review));

        await().untilTrue(isFinished);
    }

    // zip sample (Mono + Mono)
    @Test
    public void monoConcatWithFlux(){
        int productId = 10;
        Mono<ProductDto> productMono = getAProduct(productId)
            .doFinally(signal -> notifyComplete(signal))
            .map(entity -> toDto(entity));

        Mono<List<ReviewDto>> reviewsMono = productMono
            .flatMapMany( dto -> getReviewsAsync(productId))
            .collectList();

        Mono.zip(productMono, reviewsMono)
                .doFinally(signal -> notifyComplete(signal))
                .map(
                    zipped -> {
                        ProductDto dto = zipped.getT1();
                        dto.setReviewList(zipped.getT2());
                        return dto;
                    }
            ).subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    // When product is not found, exit
    @Test
    public void conditionalTermination(){
        int productId = Integer.MAX_VALUE;
        Mono<ProductDto> productMono = getAProduct(productId)
            .switchIfEmpty(Mono.error(new Exception("Product is not found")))
            .doOnError(throwable -> {
                System.out.println("Finished with error : " + throwable.getMessage());
            })
            .map(entity -> toDto(entity))
            .doOnNext(entity -> System.out.println(entity))
            ;

        Mono<List<ReviewDto>> reviewsMono = productMono
            .flatMapMany( dto -> getReviewsAsync(productId))
            .collectList();

        Mono.zip(productMono, reviewsMono)
            .doFinally(signal -> notifyComplete(signal))
            .map(
                    zipped -> {
                        ProductDto dto = zipped.getT1();
                        dto.setReviewList(zipped.getT2());
                        return dto;
                    }
            ).subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    // If only released, reviews are queried
    // This pattern comes from the following blog. (https://medium.com/netifi/conditional-logic-and-rx-f6acc0e57a48)
    @Test
    public void conditionalLogic(){
        Flux<ProductEntity> flux = getProducts();
        flux
            .doOnComplete(this::notifyComplete)
            .map(entity -> toDto(entity))
            .groupBy(dto -> {
                if(dto.isReleased()) {
                    return "released";
                } else{
                    return "not released";
                }
            })
            .flatMap(group -> {
                if(group.key().equals("released")){
                    return group.flatMap(dto -> fillReview(dto));
                }
                else{
                    return group;
                }
            })
            .subscribe(dto -> System.out.println(dto));

        await().untilTrue(isFinished);
    }

    private ProductDto toDto(ProductEntity entity){
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setReleased(entity.isReleased());
        return dto;
    }

    private Mono<ProductEntity> getAProduct(int productId){
        if(productId == Integer.MAX_VALUE){
            return Mono.empty();
        }
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setName("product 1");
        product.setPrice(1000);
        return Mono.just(product);
    }

    private Flux<ProductEntity> getProducts(){
        List<ProductEntity> productList = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            ProductEntity product = new ProductEntity();
            product.setId(i + 1);
            product.setName("product " + (i + 1));
            product.setPrice(1000);
            boolean isReleased = i == 0 ? false : true;
            product.setReleased(isReleased);
            productList.add(product);
        }
        return Flux.fromIterable(productList);
    }

    private ReviewDto getAReviewSync(int productId){
        ReviewDto review = new ReviewDto();
        review.setProductId(productId);
        review.setContents("Review 1");
        return review;
    }

    private Mono<ReviewDto> getAReviewAsync(int productId){
        ReviewDto review = new ReviewDto();
        review.setProductId(productId);
        review.setContents("Review 1");
        return Mono.just(review);
    }


    private Flux<ReviewDto> getReviewsAsync(int productId){
        System.out.println("getReviews");
        List<ReviewDto> reviewList = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            ReviewDto review = new ReviewDto();
            review.setProductId(productId);
            review.setContents("Review - " + (i + 1));
            reviewList.add(review);
        }
        return Flux.fromIterable(reviewList);
    }

    private Mono<ProductDto> fillReview(ProductDto dto){
        Mono<ProductDto> productMono = Mono.just(dto);

        Mono<List<ReviewDto>> reviewsMono = getReviewsAsync(dto.getId())
                .collectList();

        return Mono.zip(productMono, reviewsMono).map(
                zipped -> {
                    ProductDto product = zipped.getT1();
                    product.setReviewList(zipped.getT2());
                    return product;
                }
        );
    }

    private void notifyComplete(){
        isFinished.set(true);
    }

    private void notifyComplete(SignalType signal){
        isFinished.set(true);
    }

}
