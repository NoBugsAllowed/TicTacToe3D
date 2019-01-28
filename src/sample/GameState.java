package sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class GameState {
    private int n;
    private int Big = 10000;
    private Coords lastMove;
    private int isPresent[][][];
    private int value = 0;

    public List<GameState> moves = new ArrayList<GameState>();

    public GameState(int[][][] isPresent, Coords c, int n) {
        //initTab(this.isPresent,3);
        this.n = n;
        this.isPresent = new int[n][][];
        for(int i=0; i<n; i++) {
            this.isPresent[i] = new int[n][];
            for (int j = 0; j < n; j++) this.isPresent[i][j] = new int[n];
        }
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++){
                for (int k = 0; k < n; k++) this.isPresent[i][j][k] = isPresent[i][j][k];
            }
        lastMove = new Coords(c.getX(),c.getY(),c.getZ());
    }
    public Coords getLastMove() {
        return lastMove;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void InitMoves(int player){
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                for(int k=0; k<n; k++)
                {
                    if(isPresent[i][j][k]==0) {
                        if(k!=0) if(isPresent[i][j][k-1]==0) continue;
                        int[][][] tab = new int[n][][];
                        for(int i1=0; i1<n; i1++){
                            tab[i1] = new int[n][];
                            for(int j1=0; j1<n; j1++) {
                                tab[i1][j1] = new int[n];
                                for (int k1 = 0; k1 < n; k1++) tab[i1][j1][k1] = isPresent[i1][j1][k1];
                            }
                        }
                        tab[i][j][k] = player;
                        moves.add(new GameState(tab, new Coords(i,j,k),n));
                    }
                }
    }
    int RowValue(int row[]){
        int i1=0, i2=0;
        int tab[] = {0,1,20,1000,Big};
        for(int i=0; i<row.length; i++)
        {
            i1+=(row[i]==2?1:0);
            i2+=(row[i]==1?1:0);
        }
        //i1+=r2==player?1:0; i2+=r2==player?0:1;
        //i1+=r3==player?1:0; i2+=r3==player?0:1;
        if(i1!=0 && i2!=0) return 0;
        return tab[i1]+(-tab[i2]);
    }
    public void ComputeValue(){
        //liczymy poziome
        int v=0;
        for(int i=0; i<n; i++)
        {
            for(int j=0; j<n; j++){
                int v1=RowValue(new int[]{isPresent[0][j][i],isPresent[1][j][i],isPresent[2][j][i],isPresent[3][j][i]});
                int v2=RowValue(new int[]{isPresent[j][0][i],isPresent[j][1][i],isPresent[j][2][i],isPresent[j][3][i]});
                if(v1==Big || v2==Big) {value =Big; return;}
                if(v1==-Big || v2==-Big) {value =-Big; return;}
                if(i!=0){
                    v+=isPresent[0][j][i-1]!=0&&isPresent[1][j][i-1]!=0&&isPresent[2][j][i-1]!=0&&isPresent[3][j][i-1]!=0?v1:v1/3;
                    v+=isPresent[j][0][i-1]!=0&&isPresent[j][1][i-1]!=0&&isPresent[j][2][i-1]!=0&&isPresent[j][3][i-1]!=0?v2:v2/3;
                }
                else v+=v1+v2;
            }
            int v1=RowValue(new int[]{isPresent[0][0][i],isPresent[1][1][i],isPresent[2][2][i],isPresent[3][3][i]});
            int v2=RowValue(new int[]{isPresent[0][3][i],isPresent[1][2][i],isPresent[2][1][i],isPresent[3][0][i]});
            if(v1==Big || v2==Big) {value =Big; return;}
            if(v1==-Big || v2==-Big) {value =-Big; return;}
            if(i!=0) {
                v+=isPresent[0][0][i-1]!=0&&isPresent[1][1][i-1]!=0&&isPresent[2][2][i-1]!=0&&isPresent[3][3][i-1]!=0?v1:v1/3;
                v+=isPresent[0][2][i-1]!=0&&isPresent[1][1][i-1]!=0&&isPresent[2][0][i-1]!=0&&isPresent[3][0][i-1]!=0?v2:v2/3;
            }
            else v+=v1+v2;
        }
        //liczymy skośne i pionowe
        for(int i=-1; i<2; i++)
            for(int j=-1; j<2; j++)
                for(int k=0; k<n*n; k++){

                    Coords c1 = new Coords(k);
                    Coords c2 = new Coords(c1.getX()+i,c1.getY()+j,c1.getZ()+1);
                    Coords c3 = new Coords(c1.getX()+2*i,c1.getY()+2*j,c1.getZ()+2);
                    Coords c4 = new Coords(c1.getX()+3*i,c1.getY()+3*j,c1.getZ()+3);
                    if(!c2.isCorrect || !c3.isCorrect || !c4.isCorrect) continue;
                    int v1=RowValue(new int[]{isPresent[c1.getX()][c1.getY()][c1.getZ()],isPresent[c2.getX()][c2.getY()][c2.getZ()]
                            ,isPresent[c3.getX()][c3.getY()][c3.getZ()],isPresent[c4.getX()][c4.getY()][c4.getZ()]});

                    if(v1==Big||v1==-Big) {value = v1; return;}
                    v+=isPresent[c2.getX()][c2.getY()][c2.getZ()-1]!=0 && isPresent[c3.getX()][c3.getY()][c3.getZ()-1]!=0
                            &&isPresent[c4.getX()][c4.getY()][c4.getZ()-1]!=0?v1:v1/3;
                }
        value = v;
    }
    public void ComputeValue(boolean isMaximizer){
        value = isMaximizer?moves.stream().max(Comparator.comparing(GameState::getValue)).get().getValue():
                moves.stream().min(Comparator.comparing(GameState::getValue)).get().getValue();
    }

}
