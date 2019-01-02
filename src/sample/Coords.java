package sample;

public class Coords {
    int x;
    int y;
    int z;
    boolean isCorrect = true;

    public Coords(int i) {
        x = (i%9)/3;
        y = i%3;
        z = i/9;
    }
    public Coords(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        if(x<0||x>2 ||y<0||y>2||z<0||z>2) isCorrect=false;
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
        return y + 3*x + 9*z;
    }
}
