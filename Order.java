//package com.huawei.codecraft;
import java.io.PrintStream;

public class Order {

    public Order(OrderType type, int num) {
        this.type = type;
        this.num = num;
        this.velocity = 0.0;
    }

    public Order(OrderType type, int num, double velocity) {
        this.type = type;
        this.num = num;
        this.velocity = velocity;
    }

    /**
     * 此函数用于输出该指令
     * @param outStream 输出流
     */
    public void printOrder(PrintStream outStream) {
        if(type == OrderType.FORWARD || type == OrderType.ROTATE) outStream.printf("%s %d %f\n", type.getName(), num, velocity);
        else outStream.printf("%s %d\n", type.getName(), num); 
    }

    @Override
    public String toString() {
        if(type == OrderType.FORWARD || type == OrderType.ROTATE) return String.format("%s %d %f\n", type.getName(), num, velocity);
        return String.format("%s %d\n", type.getName(), num); 
    }

    private OrderType type;//指令类型
    private int num;//机器人ID[0, 3]
    private double velocity;//forward与rotate指令的速度
}
