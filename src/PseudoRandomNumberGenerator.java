import java.util.concurrent.ThreadLocalRandom;

public class PseudoRandomNumberGenerator {

    private int size;
    PseudoRandomNumberGenerator(int size) {
        this.size = size;
    }

    public int generate() {
        return ThreadLocalRandom.current().nextInt(size);
    }
}
