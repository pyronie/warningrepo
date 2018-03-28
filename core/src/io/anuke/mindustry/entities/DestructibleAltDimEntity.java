package io.anuke.mindustry.entities;

import io.anuke.ucore.entities.Damager;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.util.Mathf;

public class DestructibleAltDimEntity extends SolidAltDimEntity {
    public transient int maxhealth;
    public transient boolean dead;
    public int health;

    public void onHit(SolidEntity entity){}
    public void onDeath(){}

    public boolean isDead(){
        return dead;
    }

    @Override
    public boolean collides(SolidEntity other){
        return other instanceof Damager;
    }

    @Override
    public void collision(SolidEntity other, float x, float y){
        if(other instanceof Damager){
            onHit(other);
            damage(((Damager)other).getDamage());
        }
    }

    public void damage(int amount){
        health -= amount;
        if(health <= 0 && !dead){
            onDeath();
            dead = true;
        }
    }

    public void setMaxHealth(int health){
        maxhealth = health;
        heal();
    }

    public void clampHealth(){
        health = Mathf.clamp(health, 0, maxhealth);
    }

    public float healthfrac(){
        return (float)health/maxhealth;
    }

    public void heal(){
        dead = false;
        health = maxhealth;
    }
}