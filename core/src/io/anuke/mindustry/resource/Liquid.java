package io.anuke.mindustry.resource;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import io.anuke.ucore.util.Bundles;

public class Liquid {

	private static final Array<Liquid> liquids = new Array<>();

	public final Color color;
	public final String name;
	public final int id;

	/**0-1, 0 is completely inflammable, anything above that may catch fire when exposed to heat, 0.5+ is very flammable.*/
	public float flammability;
	/**temperature: 0.5 is 'room' temperature, 0 is very cold, 1 is molten hot*/
	public float temperature = 0.5f;
	/**how much heat this liquid can store. 0.75=water (high), anything lower is probably less dense and bad at cooling.*/
	public float heatCapacity = 0.5f;
	/**how thick this liquid is. 0.5=water (relatively viscous), 1 would be something like tar (very slow)*/
	public float viscosity = 0.5f;
	/**how prone to exploding this liquid is, when heated. 0 = nothing, 1 = nuke*/
	public float explosiveness;
	
	public Liquid(String name, Color color) {
		this.name = name;
		this.color = new Color(color);

		this.id = liquids.size;

		Liquid.liquids.add(this);
	}

	public String localizedName(){
		return Bundles.get("liquid."+ this.name + ".name");
	}

	@Override
	public String toString(){
		return localizedName();
	}

	public static Array<Liquid> getAllLiquids() {
		return Liquid.liquids;
	}

	public static Liquid getByID(int id){
		return liquids.get(id);
	}
}
