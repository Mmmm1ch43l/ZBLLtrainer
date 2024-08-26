import java.util.concurrent.ThreadLocalRandom;

public class PseudoRandomNumberGenerator {

    private final int size;
    private final int[] timeSinceLastSeen;
    private final double decay;
    PseudoRandomNumberGenerator(int size, double decay) {
        this.size = size;
        this.decay = decay;
        timeSinceLastSeen = new int[size];
        for (int i = 0; i < size; i++) {
            timeSinceLastSeen[i] = size;
        }
    }

    public int generate() {
        for (int i = 0; i < size; i++) {
            timeSinceLastSeen[i] = timeSinceLastSeen[i] + 1;
        }
        int randomIndex;
        do {
            randomIndex = ThreadLocalRandom.current().nextInt(size);
        } while (ThreadLocalRandom.current().nextDouble()<Math.pow(decay,timeSinceLastSeen[randomIndex]));
        timeSinceLastSeen[randomIndex] = 0;
        return randomIndex;
    }
}
