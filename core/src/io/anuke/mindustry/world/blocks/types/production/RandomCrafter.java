package io.anuke.mindustry.world.blocks.types.production;

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
import io.anuke.ucore.util.Mathf;

public class RandomCrafter extends Block {

    protected final int timerDump = timers++;

    protected int capacity = 20;
    protected double craftChance = 0.10;

    protected Effect createEffect = Fx.generate;

    protected Item input;
    protected Array<Item> output;

    public RandomCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        bars.add(new BlockBar(Color.GREEN, true, tile -> (float) tile.entity.totalItems() / capacity));
    }

    @Override
    public void getStats(Array<String> list) {
        super.getStats(list);

        list.add("[craftinfo]Input: " + input.name);

        for (Item item : output.items) {
            list.add("[craftinfo]Output: " + item.name);
        }

    }

    @Override
    public void update(Tile tile) {
        TileEntity ent = tile.entity();

        if (tile.entity.timer.get(timerDump, 20)) {
            tryDump(tile);
        }

        Item product = output.random();

        if (ent.getItem(product) >= capacity //output full
                || Mathf.chance(craftChance)) {
            return;
        }
        else
            ent.removeItem(input,1);

        offloadNear(tile, product);
        Effects.effect(createEffect, tile.entity);

    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return item == input && tile.entity.getItem(input) < capacity;
    }
}