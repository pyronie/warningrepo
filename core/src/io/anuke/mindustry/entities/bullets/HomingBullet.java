package io.anuke.mindustry.entities.bullets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Bullet;
import io.anuke.mindustry.entities.BulletType;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Angles;

public class HomingBullet extends BulletType {

    protected float homingSpeed = 5f;

    public HomingBullet(float speed, int damage,String name){
        super(speed,damage,name);
    }

    public void draw(Bullet b) {
        Draw.rect(b.name, b.x, b.y, b.angle());
        Draw.reset();
    }

    public void update(Bullet b) {
        Array<SolidEntity> enemies = Entities.getNearby(Vars.enemyGroup, b.x, b.y, 15);
        for (SolidEntity entity : enemies) {
            Vector2 vektor = new Vector2(Math.max(0, Math.min(1, entity.x - b.x)), Math.max(0, Math.min(1, entity.y - b.y)));
            b.setVelocity(homingSpeed, Angles.predictAngle(b.x, b.y,
                    entity.x, entity.y, /*entity.velocity.x*/1f, /*entity.velocity.y*/1f, homingSpeed));
            break;
        }
    }
}