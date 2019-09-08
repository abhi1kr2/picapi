package net.tripletwenty.picapi;

/**
 * Created by bearbob on 30.08.19.
 */
public class Position2D {
    private int posX;
    private int posY;

    public Position2D (int x, int y) {
        this.setPosX(x);
        this.setPosY(y);
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
}
