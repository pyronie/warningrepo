package io.anuke.mindustry.world.blocks.types.distribution;

import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.LiquidBlock;

public class LiquidRouter extends LiquidBlock{

	public LiquidRouter(String name) {
		super(name);
		rotate = false;
		solid = true;
		liquidFlowFactor = 2f;
		hasInventory = false;
	}
	
	@Override
	public void update(Tile tile){
		
		if(tile.entity.liquid.amount > 0){
			tryDumpLiquid(tile);
		}
	}

}
