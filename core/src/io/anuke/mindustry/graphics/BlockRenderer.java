package io.anuke.mindustry.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Layer;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.world.blocks.types.StaticBlock;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.graphics.CacheBatch;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.util.Mathf;

import java.util.Arrays;

import static io.anuke.mindustry.Vars.*;
import static io.anuke.ucore.core.Core.camera;

public class BlockRenderer{
	private final static int chunksize = 32;
	private final static int initialRequests = 32*32;
	
	private int[][][] cache;
	private CacheBatch cbatch;
	
	private Array<BlockRequest> requests = new Array<BlockRequest>(initialRequests);
	private int requestidx = 0;
	private int iterateidx = 0;
	
	public BlockRenderer(){
		for(int i = 0; i < requests.size; i ++){
			requests.set(i, new BlockRequest());
		}
	}
	
	private class BlockRequest implements Comparable<BlockRequest>{
		Tile tile;
		Layer layer;
		
		@Override
		public int compareTo(BlockRequest other){
			return layer.compareTo(other.layer);
		}

		@Override
		public String toString(){
			return tile.block().name + ":" + layer.toString();
		}
	}
	
	/**Process all blocks to draw, simultaneously drawing block shadows and static blocks.*/
	public void processBlocks(){
		requestidx = 0;
		
		int crangex = (int) (camera.viewportWidth / (chunksize * tilesize)) + 1;
		int crangey = (int) (camera.viewportHeight / (chunksize * tilesize)) + 1;
		
		int rangex = (int) (camera.viewportWidth * camera.zoom / tilesize / 2)+2;
		int rangey = (int) (camera.viewportHeight * camera.zoom / tilesize / 2)+2;
			
		int expandr = 3;
		
		Graphics.surface(renderer.shadowSurface);

		for(int x = -rangex - expandr; x <= rangex + expandr; x++){
			for(int y = -rangey - expandr; y <= rangey + expandr; y++){
				int worldx = Mathf.scl(camera.position.x, tilesize) + x;
				int worldy = Mathf.scl(camera.position.y, tilesize) + y;
				boolean expanded = (x < -rangex || x > rangex || y < -rangey || y > rangey);
				
				Tile tile = world.tile(worldx, worldy);
				
				if(tile != null){
					Block block = tile.block();
					
					if(!expanded && block != Blocks.air && world.isAccessible(worldx, worldy)){
						block.drawShadow(tile);
					}
					
					if(!(block instanceof StaticBlock)){
						if(block != Blocks.air){
							if(!expanded){
								addRequest(tile, Layer.block);
							}
						
							if(block.expanded || !expanded){
								if(block.layer != null && block.isLayer(tile)){
									addRequest(tile, block.layer);
								}
						
								if(block.layer2 != null && block.isLayer2(tile)){
									addRequest(tile, block.layer2);
								}
							}
						}
					}
				}
			}
		}
		
		Draw.color(0, 0, 0, 0.15f);
		Graphics.flushSurface();
		Draw.color();
		
		Graphics.end();
		drawCache(DrawLayer.walls, crangex, crangey);
		Graphics.begin();
		
		Arrays.sort(requests.items, 0, requestidx);
		iterateidx = 0;
	}

	public int getRequests(){
		return requestidx;
	}
	
	public void drawBlocks(Layer stopAt){
		
		for(; iterateidx < requestidx; iterateidx ++){

			if(iterateidx < requests.size - 1 && requests.get(iterateidx).layer.ordinal() > stopAt.ordinal()){
				break;
			}
			
			BlockRequest req = requests.get(iterateidx);
			Block block = req.tile.block();

			if(req.layer == Layer.block){
				block.draw(req.tile);
			}else if(req.layer == block.layer){
				block.drawLayer(req.tile);
			}else if(req.layer == block.layer2){
				block.drawLayer2(req.tile);
			}
		}
	}

	public void drawTeamBlocks(Layer layer, Team team){
		int iterateidx = this.iterateidx;

		for(; iterateidx < requestidx; iterateidx ++){

			if(iterateidx < requests.size - 1 && requests.get(iterateidx).layer.ordinal() > layer.ordinal()){
				break;
			}

			BlockRequest req = requests.get(iterateidx);
			if(req.tile.getTeam() != team) continue;
			Block block = req.tile.block();

			if(req.layer == block.layer){
				block.drawLayer(req.tile);
			}else if(req.layer == block.layer2){
				block.drawLayer2(req.tile);
			}
		}
	}

	public void skipLayer(Layer stopAt){

		for(; iterateidx < requestidx; iterateidx ++){
			if(iterateidx < requests.size - 1 && requests.get(iterateidx).layer.ordinal() > stopAt.ordinal()){
				break;
			}
		}
	}
	
	private void addRequest(Tile tile, Layer layer){
		if(requestidx >= requests.size){
			requests.add(new BlockRequest());
		}
		BlockRequest r = requests.get(requestidx);
		if(r == null){
			requests.set(requestidx, r = new BlockRequest());
		}
		r.tile = tile;
		r.layer = layer;
		requestidx ++;
	}
	
	public void drawFloor(){
		int chunksx = world.width() / chunksize, chunksy = world.height() / chunksize;

		//render the entire map
		if(cache == null || cache.length != chunksx || cache[0].length != chunksy){
			cache = new int[chunksx][chunksy][DrawLayer.values().length];

			for(DrawLayer layer : DrawLayer.values()){
				for(int x = 0; x < chunksx; x++){
					for(int y = 0; y < chunksy; y++){
						cacheChunk(x, y, layer);
					}
				}
			}
		}

		OrthographicCamera camera = Core.camera;

		if(Graphics.drawing()) Graphics.end();

		int crangex = (int)(camera.viewportWidth * camera.zoom / (chunksize * tilesize))+1;
		int crangey = (int)(camera.viewportHeight * camera.zoom / (chunksize * tilesize))+1;

		DrawLayer[] layers = DrawLayer.values();

		for(int i = 0; i < layers.length - 1; i ++) {
			drawCache(layers[i], crangex, crangey);
		}

		Graphics.begin();

		Draw.reset();

		if(debug && debugChunks){
			Draw.color(Color.YELLOW);
			Lines.stroke(1f);
			for(int x = -crangex; x <= crangex; x++){
				for(int y = -crangey; y <= crangey; y++){
					int worldx = Mathf.scl(camera.position.x, chunksize * tilesize) + x;
					int worldy = Mathf.scl(camera.position.y, chunksize * tilesize) + y;

					if(!Mathf.inBounds(worldx, worldy, cache))
						continue;
					Lines.rect(worldx * chunksize * tilesize, worldy * chunksize * tilesize, chunksize * tilesize, chunksize * tilesize);
				}
			}
			Draw.reset();
		}
	}

	void drawCache(DrawLayer layer, int crangex, int crangey){
		Gdx.gl.glEnable(GL20.GL_BLEND);

		layer.begin(cbatch);

		for(int x = -crangex; x <= crangex; x++){
			for(int y = -crangey; y <= crangey; y++){
				int worldx = Mathf.scl(camera.position.x, chunksize * tilesize) + x;
				int worldy = Mathf.scl(camera.position.y, chunksize * tilesize) + y;

				if(!Mathf.inBounds(worldx, worldy, cache))
					continue;

				cbatch.drawCache(cache[worldx][worldy][layer.ordinal()]);
			}
		}

		layer.end(cbatch);
	}

	void cacheChunk(int cx, int cy, DrawLayer layer){
		if(cbatch == null){
			createBatch();
		}

		cbatch.begin();
		Graphics.useBatch(cbatch);

		for(int tilex = cx * chunksize; tilex < (cx + 1) * chunksize; tilex++){
			for(int tiley = cy * chunksize; tiley < (cy + 1) * chunksize; tiley++){
				Tile tile = world.tile(tilex, tiley);
				if(tile == null) continue;

				if(tile.floor().drawLayer == layer && tile.block().drawLayer != DrawLayer.walls){
					tile.floor().draw(tile);
				}else if(tile.floor().drawLayer.ordinal() < layer.ordinal() && tile.block().drawLayer != DrawLayer.walls){
					tile.floor().drawNonLayer(tile);
				}

				if(tile.block().drawLayer == layer && layer == DrawLayer.walls){
					tile.block().draw(tile);
				}
			}
		}
		Graphics.popBatch();
		cbatch.end();
		cache[cx][cy][layer.ordinal()] = cbatch.getLastCache();
	}

	public void clearTiles(){
		cache = null;
		createBatch();
	}

	private void createBatch(){
		if(cbatch != null)
			cbatch.dispose();
		cbatch = new CacheBatch(world.width() * world.height() * 4);
	}
}
