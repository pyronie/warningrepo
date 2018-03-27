package io.anuke.mindustry.entities;

import com.badlogic.gdx.math.Rectangle;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.Hitbox;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.util.QuadTree;

public class SolidAltDimEntity extends AltDimEntity implements QuadTree.QuadTreeObject {
    public transient Hitbox hitbox = new Hitbox(10f);
    public transient Hitbox hitboxTile = new Hitbox(4f);
    public transient float lastX = Float.NaN, lastY = Float.NaN;

    public void move(float x, float y){
        Entities.collisions().move(this, x, y);
    }

    public boolean collidesTile(){
        return Entities.collisions().overlapsTile(hitbox.getRect(x, y));
    }

    public boolean collides(SolidEntity other){
        return true;
    }

    public void collision(SolidEntity other, float x, float y){}

    @Override
    public void getBoundingBox(Rectangle out){
        hitbox.getRect(out, x, y);
    }
}