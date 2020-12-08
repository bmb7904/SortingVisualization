package bernardi;

import javafx.scene.shape.Rectangle;

public class myRectangle extends Rectangle {
    private int value;

    public myRectangle(int v)
    {
        super();
        this.value = v;
    }

    public int getValue()
    {
        return value;
    }
}
