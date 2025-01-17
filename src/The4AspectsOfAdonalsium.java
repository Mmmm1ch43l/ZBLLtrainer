public class The4AspectsOfAdonalsium {
    public static final String[] SHARDS = {     "Ambition", "Autonomy", "Cultivation", "Devotion",
                                                "Dominion", "Endowment", "Honor", "Invention",
                                                "Mercy", "Odium", "Preservation", "Reason",
                                                "Ruin", "Valor", "Virtuosity", "Whimsy"};

    public static final int x = 0;

    public static int[][] COMPATIBILITY = {
            {x,30,40,-25,           50,25,-15,60,           -40,15,-30,5,           20,10,45,-35},          // Ambition
            {x,x,15,-80,            25,-5,3,25,             5,20,-20,10,            40,15,18,60},           // Autonomy
            {x,x,x,22,              3,35,-20,70,            8,-5,1,-10,             16,-2,20,15},           // Cultivation
            {x,x,x,x,               28,13,65,5,             12,38,30,-23,           -20,3,55,-5},           // Devotion
            {x,x,x,x,               x,20,11,-7,             15,4,18,-6,             32,-3,-15,-23},         // Dominion
            {x,x,x,x,               x,x,-10,36,             13,-18,21,11,           2,7,80,27},             // Endowment
            {x,x,x,x,               x,x,x,-25,              25,15,13,18,            -12,85,8,-13},          // Honor
            {x,x,x,x,               x,x,x,x,                10,-19,-16,51,          35,3,26,29},            // Invention
            {x,x,x,x,               x,x,x,x,                x,-53,21,16,            -8,9,1,5},              // Mercy
            {x,x,x,x,               x,x,x,x,                x,x,-30,-5,             16,25,-11,-7},          // Odium
            {x,x,x,x,               x,x,x,x,                x,x,x,3,                -13,-26,-25,-30},       // Preservation
            {x,x,x,x,               x,x,x,x,                x,x,x,x,                -3,-2,3,7},             // Reason
            {x,x,x,x,               x,x,x,x,                x,x,x,x,                x,-30,-25,3},           // Ruin
            {x,x,x,x,               x,x,x,x,                x,x,x,x,                x,x,42,-3},             // Valor
            {x,x,x,x,               x,x,x,x,                x,x,x,x,                x,x,x,11},              // Virtuosity
            {x,x,x,x,               x,x,x,x,                x,x,x,x,                x,x,x,x},               // Whimsy
    };

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < i; j++) {
                COMPATIBILITY[i][j] = COMPATIBILITY[i][j];
            }
        }
        int[][] partition = {{0,1,2},{0,1,2},{0,1,2}};
        int cutoffScore = -10000;
        int[] bestScores = new int[]{-10000,-10000,-10000,-10000,-10000};
        int[][][] bestPartitions = new int[][][]{{{0,1,2},{0,1,2},{0,1,2}},{{0,1,2},{0,1,2},{0,1,2}},{{0,1,2},{0,1,2},{0,1,2}},{{0,1,2},{0,1,2},{0,1,2}},{{0,1,2},{0,1,2},{0,1,2}}};
        int counter = 0;
        while (partition != null) {
            if (counter++ % 10000 == 0) System.out.println(counter - 1);
            int[][] expandedPartition = expandPartition(partition);
            int score = 0;
            for (int i = 0; i < 4; i++) {
                score += COMPATIBILITY[expandedPartition[i][0]][expandedPartition[i][1]] + COMPATIBILITY[expandedPartition[i][0]][expandedPartition[i][2]] +
                        COMPATIBILITY[expandedPartition[i][0]][expandedPartition[i][3]] + COMPATIBILITY[expandedPartition[i][1]][expandedPartition[i][2]] +
                        COMPATIBILITY[expandedPartition[i][1]][expandedPartition[i][3]] + COMPATIBILITY[expandedPartition[i][2]][expandedPartition[i][3]];
            }
            if (score > cutoffScore){
                cutoffScore = score;
                bestScores[0] = score;
                bestPartitions[0] = new int[][]{partition[0].clone(),partition[1].clone(),partition[2].clone()};
                for (int i = 1; i < 5; i++) {
                    if (score > bestScores[i]){
                        bestScores[i-1] = bestScores[i];
                        bestPartitions[i-1] = new int[][]{bestPartitions[i][0].clone(),bestPartitions[i][1].clone(),bestPartitions[i][2].clone()};
                        bestScores[i] = score;
                        bestPartitions[i] = new int[][]{partition[0].clone(),partition[1].clone(),partition[2].clone()};
                    }
                }
            }
            partition = nextPartition(partition);
        }
        System.out.println("\nBest partitions are:");
        for (int i = 0; i < 5; i++) {
            System.out.println((5-i) + ": With a score of " + bestScores[i]);
            printPartition(expandPartition(bestPartitions[i]));
        }
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
