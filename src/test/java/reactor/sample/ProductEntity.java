package reactor.sample;

public class ProductEntity {
    private int id;
    private String name;
    private int price;
    private boolean isReleased;

    public boolean isReleased() {
        return isReleased;
    }

    public void setReleased(boolean released) {
        isReleased = released;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
