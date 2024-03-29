package io.anuke.mindustry.world.blocks.types.power;

import com.badlogic.gdx.math.GridPoint2;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Fx;
import io.anuke.mindustry.world.Edges;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.PowerBlock;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.util.Mathf;

public class PowerGenerator extends PowerBlock {

    public PowerGenerator(String name) {
        super(name);
    }

    protected void distributePower(Tile tile){
        TileEntity entity = tile.entity;
        int sources = 0;

        for(GridPoint2 point : Edges.getEdges(size)){
            Tile target = tile.getNearby(point);
            if(target != null && target.block().hasPower &&
                    shouldDistribute(tile, target)) sources ++;
        }

        if(sources == 0) return;

        float result = entity.power.amount / sources;

        for(GridPoint2 point : Edges.getEdges(size)){
            Tile target = tile.getNearby(point);
            if(target == null) continue;
            target = target.target();

            if(target.block().hasPower && shouldDistribute(tile, target)){
                float transmit = Math.min(result * Timers.delta(), entity.power.amount);
                if(target.block().acceptPower(target, tile, transmit)){
                    entity.power.amount -= target.block().addPower(target, transmit);
                }
            }
        }
    }

    protected boolean shouldDistribute(Tile tile, Tile other){
        if(other.block() instanceof PowerGenerator){
            return other.entity.power.amount / other.block().powerCapacity <
                    tile.entity.power.amount / powerCapacity;
        }
        return true;
    }

    @Override
    public void update(Tile tile) {
        distributePower(tile);
    }

    @Override
    public void onDestroyed(Tile tile){
        float x = tile.worldx(), y = tile.worldy();

        Effects.effect(Fx.shellsmoke, x, y);
        Effects.effect(Fx.blastsmoke, x, y);

        Timers.run(Mathf.random(8f + Mathf.random(6f)), () -> {
            Effects.shake(6f, 8f, x, y);
            Effects.effect(Fx.generatorexplosion, x, y);
            Effects.effect(Fx.shockwave, x, y);

            //TODO better explosion effect!

            Effects.sound(explosionSound, x, y);
        });
    }

    @Override
    public TileEntity getEntity() {
        return new GeneratorEntity();
    }

    public static class GeneratorEntity extends TileEntity{
        public float generateTime;
    }
}
