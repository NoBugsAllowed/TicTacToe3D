package sample;

public class Coords {
    private static final int N = 4;
    private int x;
    private int y;
    private int z;
    boolean isCorrect = true;

    public Coords(int i) {
        x = (i%(N*N))/N;
        y = i%N;
        z = i/(N*N);
    }
    public Coords(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        if(x<0||x>N-1 ||y<0||y>N-1||z<0||z>N-1) isCorrect=false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    public int getIndex(){
        return y + N*x + N*N*z;
    }
}