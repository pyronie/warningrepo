package io.anuke.mindustry.core;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.ai.Pathfind;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.io.Map;
import io.anuke.mindustry.io.MapIO;
import io.anuke.mindustry.io.Maps;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.WorldGenerator;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.Entity;
import io.anuke.ucore.modules.Module;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Tmp;

import static io.anuke.mindustry.Vars.tilesize;

public class World extends Module{
	private int seed;
	
	private Map currentMap;
	private Tile[][] tiles;
	private Pathfind pathfind = new Pathfind();
	private Maps maps = new Maps();

	private Array<Tile> tempTiles = new Array<>();
	
	public World(){
		maps.load();
	}
	
	@Override
	public void dispose(){
		maps.dispose();
	}
	
	public Maps maps(){
		return maps;
	}

	public Pathfind pathfinder(){
		return pathfind;
	}

    //TODO proper spawnpoints!
	public float getSpawnX(){
		return width() * tilesize/2f;
	}

	public float getSpawnY(){
		return height() * tilesize/2f;
	}
	
	public boolean solid(int x, int y){
		Tile tile = tile(x, y);
		
		return tile == null || tile.solid();
	}
	
	public boolean passable(int x, int y){
		Tile tile = tile(x, y);
		
		return tile != null && tile.passable();
	}
	
	public boolean wallSolid(int x, int y){
		Tile tile = tile(x, y);
		return tile == null || tile.block().solid;
	}
	
	public boolean isAccessible(int x, int y){
		return !wallSolid(x, y-1) || !wallSolid(x, y+1) || !wallSolid(x-1, y) ||!wallSolid(x+1, y);
	}
	
	public boolean blends(Block block, int x, int y){
		return !floorBlends(x, y-1, block) || !floorBlends(x, y+1, block) 
				|| !floorBlends(x-1, y, block) ||!floorBlends(x+1, y, block);
	}
	
	public boolean floorBlends(int x, int y, Block block){
		Tile tile = tile(x, y);
		return tile == null || tile.floor().id <= block.id;
	}
	
	public Map getMap(){
		return currentMap;
	}
	
	public int width(){
		return currentMap.meta.width;
	}
	
	public int height(){
		return currentMap.meta.height;
	}

	public Tile tile(int packed){
		return tile(packed % width(), packed / width());
	}
	
	public Tile tile(int x, int y){
		if(tiles == null){
			return null;
		}
		if(!Mathf.inBounds(x, y, tiles)) return null;
		return tiles[x][y];
	}
	
	public Tile tileWorld(float x, float y){
		return tile(Mathf.scl2(x, tilesize), Mathf.scl2(y, tilesize));
	}

	public int toTile(float coord){
		return Mathf.scl2(coord, tilesize);
	}
	
	public Tile[][] getTiles(){
		return tiles;
	}
	
	private void clearTileEntities(){
		for(int x = 0; x < tiles.length; x ++){
			for(int y = 0; y < tiles[0].length; y ++){
				if(tiles[x][y] != null && tiles[x][y].entity != null){
					tiles[x][y].entity.remove();
				}
			}
		}
	}

	/**Resizes the tile array to the specified size and returns the resulting tile array.
     * Only use for loading saves!*/
    public Tile[][] createTiles(int width, int height){
        if(tiles != null){
            clearTileEntities();

            if(tiles.length != width || tiles[0].length != height){
                tiles = new Tile[width][height];
            }
        }else{
            tiles = new Tile[width][height];
        }

        return tiles;
    }

    public void setMap(Map map){
    	this.currentMap = map;
	}
	
	public void loadMap(Map map){
		loadMap(map, MathUtils.random(0, 999999));
	}
	
	public void loadMap(Map map, int seed){
		this.currentMap = map;
		this.seed = seed;

		int width = map.meta.width, height = map.meta.height;
		
		createTiles(width, height);
		
		Entities.resizeTree(0, 0, width * tilesize, height * tilesize);
		
		WorldGenerator.generate(tiles, MapIO.readTileData(map));
	}

	void set(int x, int y, Block type, int rot){
		if(!Mathf.inBounds(x, y, tiles)){
			return;
		}
		if(type == ProductionBlocks.stonedrill){
			tiles[x][y].setFloor(Blocks.stone);
		}
		tiles[x][y].setBlock(type, rot);
	}

	public int getSeed(){
		return seed;
	}

	public void removeBlock(Tile tile){
		if(!tile.block().isMultiblock() && !tile.isLinked()){
			tile.setBlock(Blocks.air);
		}else{
			Tile target = tile.target();
			Array<Tile> removals = target.getLinkedTiles(tempTiles);
			for(Tile toremove : removals){
				//note that setting a new block automatically unlinks it
				toremove.setBlock(Blocks.air);
			}
		}
	}
	
	public TileEntity findTileTarget(float x, float y, Tile tile, float range, boolean damaged){
		Entity closest = null;
		float dst = 0;
		
		int rad = (int)(range/tilesize)+1;
		int tilex = Mathf.scl2(x, tilesize);
		int tiley = Mathf.scl2(y, tilesize);
		
		for(int rx = -rad; rx <= rad; rx ++){
			for(int ry = -rad; ry <= rad; ry ++){
				Tile other = tile(rx+tilex, ry+tiley);
				
				if(other != null && other.getLinked() != null){
					other = other.getLinked();
				}
				
				if(other == null || other.entity == null || (tile != null && other.entity == tile.entity)) continue;
				
				TileEntity e = other.entity;
				
				if(damaged && e.health >= e.tile.block().health)
					continue;
				
				float ndst = Vector2.dst(x, y, e.x, e.y);
				if(ndst < range && (closest == null || ndst < dst)){
					dst = ndst;
					closest = e;
				}
			}
		}

		return (TileEntity) closest;
	}

	/**Raycast, but with world coordinates.*/
	public GridPoint2 raycastWorld(float x, float y, float x2, float y2){
		return raycast(Mathf.scl2(x, tilesize), Mathf.scl2(y, tilesize),
				Mathf.scl2(x2, tilesize), Mathf.scl2(y2, tilesize));
	}
	
	/**
	 * Input is in block coordinates, not world coordinates.
	 * @return null if no collisions found, block position otherwise.*/
	public GridPoint2 raycast(int x0f, int y0f, int x1, int y1){
		int x0 = x0f;
		int y0 = y0f;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;
		int e2;
		while(true){

			if(!passable(x0, y0)){
				return Tmp.g1.set(x0, y0);
			}
			if(x0 == x1 && y0 == y1) break;

			e2 = 2 * err;
			if(e2 > -dy){
				err = err - dy;
				x0 = x0 + sx;
			}

			if(e2 < dx){
				err = err + dx;
				y0 = y0 + sy;
			}
		}
		return null;
	}

	public void raycastEach(int x0f, int y0f, int x1, int y1, Raycaster cons){
		int x0 = x0f;
		int y0 = y0f;
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);

		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;
		int e2;
		while(true){

			if(cons.accept(x0, y0)) break;
			if(x0 == x1 && y0 == y1) break;

			e2 = 2 * err;
			if(e2 > -dy){
				err = err - dy;
				x0 = x0 + sx;
			}

			if(e2 < dx){
				err = err + dx;
				y0 = y0 + sy;
			}
		}
	}

	public interface Raycaster{
		boolean accept(int x, int y);
	}
}
