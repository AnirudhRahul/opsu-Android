package itdelatrisu.opsu.user;

import fluddokt.opsu.fake.Color;

/**
 * Created by user on 12/28/2018.
 */

public class FunctionButton extends UserButton {


    public FunctionButton(int x, int y, Color color, String function) {
        super(x, y, color);
        setPosition(x, y);
        this.function = function;
    }
}
