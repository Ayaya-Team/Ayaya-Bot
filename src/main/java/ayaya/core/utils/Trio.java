package ayaya.core.utils;

public class Trio<T, R, S> {

    private T first;
    private R second;
    private S third;

    public Trio(T first, R second, S third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }

    public S getThird() {
        return third;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(R second) {
        this.second = second;
    }

    public void setThird(S third) {
        this.third = third;
    }
}