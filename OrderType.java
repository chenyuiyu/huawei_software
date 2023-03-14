import org.junit.Test;

public enum OrderType {
    FORWARD("forward"), ROTATE("rotate"), BUY("buy"), SELL("sell"), DESTROY("destroy");

    private OrderType(String name) {
        this.name = name;
    }

    /**
     * 获取指令名
     * @return 指令名
     */

    public String getName() {
        return this.name;
    }

    private final String name;
}