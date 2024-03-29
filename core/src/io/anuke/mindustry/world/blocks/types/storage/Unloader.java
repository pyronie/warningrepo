package io.anuke.mindustry.world.blocks.types.storage;

import io.anuke.mindustry.resource.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.BlockGroup;
import io.anuke.mindustry.world.Tile;

public class Unloader extends Block {
    protected final int timerUnload = timers++;
    protected int speed = 5;

    public Unloader(String name){
        super(name);
        update = true;
        solid = true;
        health = 70;
        group = BlockGroup.transportation;
    }

    @Override
    public void update(Tile tile){
        if(tile.entity.inventory.totalItems() == 0 && tile.entity.timer.get(timerUnload, speed)){
            tile.allNearby(other -> {
                if(other.block() instanceof StorageBlock && tile.entity.inventory.totalItems() == 0 &&
                        ((StorageBlock)other.block()).hasItem(other, null)){
                    offloadNear(tile, ((StorageBlock)other.block()).removeItem(other, null));
                }
            });
        }

        if(tile.entity.inventory.totalItems() > 0){
            tryDump(tile);
        }
    }

    @Override
    public boolean canDump(Tile tile, Tile to, Item item) {
        Block block = to.target().block();
        return !(block instanceof StorageBlock);
    }

    @Override
    public void setBars(){}
}
