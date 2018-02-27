package io.anuke.mindustry.world.blocks.types.production;

import java.util.Arrays;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Fx;
import io.anuke.mindustry.resource.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.BlockBar;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;

public class Crafter extends Block{

    protected final int timerDump = timers++;
    protected final int timerCraft = timers++;

    protected int capacity = 20;

    protected float craftTime = 20f; //time to craft one item, so max 3 items per second by default
    protected Effect craftEffect = Fx.smelt;


    protected Map<Item,Item[]> recipe;

    public Crafter(String name) {
        super(name);
        update = true;
        solid = true;
        bars.add(new BlockBar(Color.GREEN, true, tile -> (float)tile.entity.totalItems()/capacity));
    }

    @Override
    public void getStats(Array<String> list){
        super.getStats(list);

        for(Item item : recipe.keySet()) {
            list.add("[craftinfo]Input: " + Arrays.toString(recipe.get(item)));
            list.add("[craftinfo]Output: " + item);
        }

    }

    @Override
    public void update(Tile tile){
        boolean recipeFilled;

        CrafterEntity ent = tile.entity();

        for(Item result : recipe.keySet()) {
            recipeFilled = true;
            if (tile.entity.timer.get(timerDump, 20) && tile.entity.hasItem(result)) {
                tryDump(tile, -1, result);
            }

            for (Item item : recipe.get(result)) {
                if (!tile.entity.hasItem(item)) {
                    recipeFilled = false;
                    break;
                }
            }
            if(recipeFilled) {
                for (Item item : recipe.get(result)) {
                    tile.entity.removeItem(item, 1);
                }
                if(ent.getItem(result) >= capacity //output full
                        || !ent.timer.get(timerCraft, craftTime)){ //not yet time
                    return;
                }

                offloadNear(tile, result);
                Effects.effect(craftEffect, tile.entity);
            }
        }
    }

    @Override
    public boolean acceptItem(Item item, Tile dest, Tile source){
        boolean craft = false;
        for(Item key : recipe.keySet()) {
            for (Item req : recipe.get(key)) {
                if (item == req) {
                    craft = true;
                    break;
                }
            }
        }
        return craft;
    }

    @Override
    public TileEntity getEntity() {
        return new CrafterEntity();
    }

    public class CrafterEntity extends TileEntity{
    }
}