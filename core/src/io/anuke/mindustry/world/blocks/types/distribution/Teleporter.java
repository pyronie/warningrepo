package io.anuke.mindustry.world.blocks.types.distribution;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.resource.Item;
import io.anuke.mindustry.world.BarType;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.PowerBlock;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.scene.ui.ButtonGroup;
import io.anuke.ucore.scene.ui.ImageButton;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Strings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static io.anuke.mindustry.Vars.syncBlockState;

public class Teleporter extends PowerBlock{
	public static final Color[] colorArray = {Color.ROYAL, Color.ORANGE, Color.SCARLET, Color.FOREST,
			Color.PURPLE, Color.GOLD, Color.PINK, Color.BLACK};
	public static final int colors = colorArray.length;

	private static ObjectSet<Tile>[] teleporters = new ObjectSet[colors];
	private static byte lastColor = 0;

	private Array<Tile> removal = new Array<>();
	private Array<Tile> returns = new Array<>();

	protected float powerPerItem = 0.8f;

	static{
		for(int i = 0; i < colors; i ++){
			teleporters[i] = new ObjectSet<>();
		}
	}
	
	public Teleporter(String name) {
		super(name);
		update = true;
		solid = true;
		health = 80;
		powerCapacity = 30f;
		size = 3;
	}

	@Override
	public void configure(Tile tile, byte data) {
		TeleporterEntity entity = tile.entity();
		if(entity != null){
			entity.color = data;
			entity.inventory.clear();
		}
	}

	@Override
	public void setBars(){
		super.setBars();
		bars.remove(BarType.inventory);
	}

	@Override
	public void setStats(){
		super.setStats();
		stats.add("poweritem", Strings.toFixed(powerPerItem, 1));
	}

	@Override
	public void placed(Tile tile){
		tile.<TeleporterEntity>entity().color = lastColor;
		setConfigure(tile, lastColor);
	}
	
	@Override
	public void draw(Tile tile){
		TeleporterEntity entity = tile.entity();
		
		super.draw(tile);
		
		Draw.color(colorArray[entity.color]);
		Draw.rect("teleporter-top", tile.drawx(), tile.drawy());
		//Draw.color(Color.WHITE);
		//Draw.alpha(0.45f + Mathf.absin(Timers.time(), 7f, 0.26f));
		//Draw.rect("teleporter-top", tile.worldx(), tile.worldy());
		Draw.reset();
	}
	
	@Override
	public void update(Tile tile){
		TeleporterEntity entity = tile.entity();

		teleporters[entity.color].add(tile);

		if(entity.inventory.totalItems() > 0){
			tryDump(tile);
		}
	}

	@Override
	public boolean isConfigurable(Tile tile){
		return true;
	}
	
	@Override
	public void buildTable(Tile tile, Table table){
		TeleporterEntity entity = tile.entity();

		ButtonGroup<ImageButton> group = new ButtonGroup<>();
		Table cont = new Table();
		cont.margin(4);
		cont.marginBottom(5);

		cont.add().colspan(4).height(105f);
		cont.row();

		for(int i = 0; i < colors; i ++){
			final int f = i;
			ImageButton button = cont.addImageButton("white", "toggle", 24, () -> {
				lastColor = (byte)f;
				setConfigure(tile, (byte)f);
			}).size(34, 38).padBottom(-5.1f).group(group).get();
			button.getStyle().imageUpColor = colorArray[f];
			button.setChecked(entity.color == f);

			if(i%4 == 3){
				cont.row();
			}
		}

		table.add(cont);
	}
	
	@Override
	public void handleItem(Item item, Tile tile, Tile source){
		TeleporterEntity entity = tile.entity();

		Array<Tile> links = findLinks(tile);
		
		if(links.size > 0){
            if(!syncBlockState || Net.server() || !Net.active()){
                Tile target = links.random();
                target.entity.inventory.addItem(item, 1);
            }
		}

		entity.power.amount -= powerPerItem;
	}
	
	@Override
	public boolean acceptItem(Item item, Tile tile, Tile source){
		TeleporterEntity entity = tile.entity();
		return !(source.block() instanceof Teleporter) && entity.power.amount >= powerPerItem && findLinks(tile).size > 0;
	}
	
	@Override
	public TileEntity getEntity(){
		return new TeleporterEntity();
	}
	
	private Array<Tile> findLinks(Tile tile){
		TeleporterEntity entity = tile.entity();
		
		removal.clear();
		returns.clear();
		
		for(Tile other : teleporters[entity.color]){
			if(other != tile){
				if(other.block() instanceof Teleporter){
					if(other.<TeleporterEntity>entity().color != entity.color){
						removal.add(other);
					}else if(other.entity.inventory.totalItems() == 0){
						returns.add(other);
					}
				}else{
					removal.add(other);
				}
			}
		}

		for(Tile remove : removal)
			teleporters[entity.color].remove(remove);
		
		return returns;
	}

	public static class TeleporterEntity extends TileEntity{
		public byte color = 0;
		
		@Override
		public void write(DataOutputStream stream) throws IOException{
			stream.writeByte(color);
		}
		
		@Override
		public void read(DataInputStream stream) throws IOException{
			color = stream.readByte();
		}
	}

}
