import java.util.Arrays;

public class The4AspectsOfAdonalsium {
    public static final String[] SHARDS = {     "Ambition", "Autonomy", "Cultivation", "Devotion",
                                                "Dominion", "Endowment", "Honor", "Invention",
                                                "Mercy", "Odium", "Preservation", "Reason",
                                                "Ruin", "Valor", "Virtuosity", "Whimsy"};

    public static final int x = 0;

    public static int[][] COMPATIBILITY = {
            {x,0,0,0,               0,0,0,0,                0,0,0,0,                0,0,0,0},               // Ambition
            {0,x,0,0,               0,0,0,0,                0,0,0,0,                0,0,0,0},               // Autonomy
            {0,0,x,0,               0,0,0,0,                0,0,0,0,                0,0,0,0},               // Cultivation
            {0,0,0,x,               0,0,0,0,                0,0,0,0,                0,0,0,0},               // Devotion
            {0,0,0,0,               x,0,0,0,                0,0,0,0,                0,0,0,0},               // Dominion
            {0,0,0,0,               0,x,0,0,                0,0,0,0,                0,0,0,0},               // Endowment
            {0,0,0,0,               0,0,x,0,                0,0,0,0,                0,0,0,0},               // Honor
            {0,0,0,0,               0,0,0,x,                0,0,0,0,                0,0,0,0},               // Invention
            {0,0,0,0,               0,0,0,0,                x,0,0,0,                0,0,0,0},               // Mercy
            {0,0,0,0,               0,0,0,0,                0,x,0,0,                0,0,0,0},               // Odium
            {0,0,0,0,               0,0,0,0,                0,0,x,0,                0,0,0,0},               // Preservation
            {0,0,0,0,               0,0,0,0,                0,0,0,x,                0,0,0,0},               // Reason
            {0,0,0,0,               0,0,0,0,                0,0,0,0,                x,0,0,0},               // Ruin
            {0,0,0,0,               0,0,0,0,                0,0,0,0,                0,x,0,0},               // Valor
            {0,0,0,0,               0,0,0,0,                0,0,0,0,                0,0,x,0},               // Virtuosity
            {0,0,0,0,               0,0,0,0,                0,0,0,0,                0,0,0,x},               // Whimsy
    };

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < i; j++) {
                COMPATIBILITY[i][j] = COMPATIBILITY[i][j];
            }
        }
        int[][] partition = {{0,1,2},{0,1,2},{0,1,2}};
        int bestScore = -10000;
        int[][] bestPartition = null;
        int counter = 0;
        while (partition != null) {
            if (counter++ % 10000 == 0) System.out.println(counter);
            int[][] expandedPartition = expandPartition(partition);
            int score = 0;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < ; j++) {

                }
            }
            if (score > bestScore){
                bestScore = score;
                bestPartition = new int[][]{partition[0].clone(),partition[1].clone(),partition[2].clone()};
            }
            partition = nextPartition(partition);
        }
        System.out.println("Best partition is:");
        printPartition(expandPartition(bestPartition));
        System.out.println("With a score of " + bestScore);
    }

    public static int[] nextSubset(int[] input, int size){
        int[] output = input.clone();
        for (int i = 0; i < output.length; i++) {
            if (output[output.length-i-1] < size - i - 1){
                output[output.length-i-1] += 1;
                for (int j = 1; j < i + 1; j++) {
                    output[output.length-i-1 + j] = output[output.length-i-1] + j;
                }
                return output;
            }
        }
        return null;
    }

    public static int[][] nextPartition(int[][] input){
        int[][] output = new int[input.length][];
        for (int i = 0; i < output.length; i++) {
            output[output.length-i-1] = nextSubset(input[output.length-i-1],15 - 4*(output.length-i-1));
            if (output[output.length-i-1] != null){
                for (int j = 0; j < output.length-i-1; j++) {
                    output[j] = input[j].clone();
                }
                for (int j = 1; j < i + 1; j++) {
                    output[output.length-i-1 + j] = new int[]{0,1,2};
                }
                return output;
            }
        }
        return null;
    }

    public static int[][] expandPartition(int[][] input){
        int[][] output = new int[4][];
        boolean[] used = new boolean[16];
        int temp;
        for (int i = 0; i < 4; i++) {
            output[i] = new int[4];
            temp = 0;
            while (used[temp]) temp++;
            used[temp] = true;
            if (i == 3){
                output[i] = new int[]{temp,0,1,2};
            } else {
                output[i][0] = temp;
                System.arraycopy(input[i], 0, output[i], 1, 3);
            }
            for (int j = 0; j < 3; j++) {
                temp = -1;
                while (output[i][j+1]>-1){
                    if (!used[++temp]) output[i][j+1]--;
                }
                output[i][j+1] = temp;
            }
            for (int j = 0; j < 3; j++) {
                used[output[i][j+1]] = true;
            }
        }
        return output;
    }

    public static void printPartition(int[][] input){
        for (int[] ints : input) {
            System.out.println(SHARDS[ints[0]] + ", " + SHARDS[ints[1]] + ", " + SHARDS[ints[2]] + ", " + SHARDS[ints[3]]);
        }
        System.out.println();
    }
}
