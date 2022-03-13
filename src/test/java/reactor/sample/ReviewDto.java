package reactor.sample;

public class ReviewDto {
    private int productId;
    private String contents;

    @Override
    public String toString() {
        return "ReviewDto{" +
                "productId=" + productId +
                ", contents='" + contents + '\'' +
                '}';
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
