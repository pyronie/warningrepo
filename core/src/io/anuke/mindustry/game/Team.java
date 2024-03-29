package io.anuke.mindustry.game;

import com.badlogic.gdx.graphics.Color;

public enum Team {
    none(Color.DARK_GRAY),
    blue(Color.ROYAL),
    red(Color.valueOf("e84737")),
    green(Color.valueOf("1dc645"));

    public final Color color;

    Team(Color color){
        this.color = color;
    }
}
