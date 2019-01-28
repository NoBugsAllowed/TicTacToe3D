package sample;

public class Game {
    private int[][][] fields;

    public Game() {
        fields = new int[3][3][3];
    }

    public boolean placeBall(int i, int j, int player) {
        int k = 0;
        while (k < 3 && fields[i][j][k] != 0)
            k++;
        if (k == 3)
            return false;
        else {
            fields[i][j][k] = player;
        }
        return true;
    }
}
