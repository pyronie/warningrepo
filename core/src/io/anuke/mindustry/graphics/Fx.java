package io.anuke.mindustry.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.graphics.*;
import io.anuke.ucore.util.Angles;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.respawnduration;
import static io.anuke.mindustry.Vars.tilesize;

public class Fx{
	public static Color lightRed = Hue.mix(Color.WHITE, Color.FIREBRICK, 0.1f);
    public static Color lightOrange = Color.valueOf("f68021");
    public static Color lighterOrange = Color.valueOf("f6e096");
    public static Color whiteOrange = Hue.mix(lightOrange, Color.WHITE, 0.6f);
    public static Color whiteYellow = Hue.mix(Color.YELLOW, Color.WHITE, 0.6f);
    public static Color lightGray = Color.valueOf("b0b0b0");
	public static Color glowy = Color.valueOf("fdc056");
	public static Color beam = Color.valueOf("9bffbe");
	public static Color beamLight = Color.valueOf("ddffe9");
	
	public static final Effect
	
	generatorexplosion = new Effect(28, 40f, e -> {
		Angles.randLenVectors(e.id, 16, 10f + e.ifract()*8f, (x, y)->{
			float size = e.fract()*12f + 1f;
			Draw.color(Color.WHITE, lightOrange, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	reactorsmoke = new Effect(17, e -> {
		Angles.randLenVectors(e.id, 4, e.ifract()*8f, (x, y)->{
			float size = 1f+e.fract()*5f;
			Draw.color(Color.LIGHT_GRAY, Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	nuclearsmoke = new Effect(40, e -> {
		Angles.randLenVectors(e.id, 4, e.ifract()*13f, (x, y)->{
			float size = e.sfract()*4f;
			Draw.color(Color.LIGHT_GRAY, Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	nuclearcloud = new Effect(90, 200f, e -> {
		Angles.randLenVectors(e.id, 10, e.powfract()*90f, (x, y)->{
			float size = e.fract()*14f;
			Draw.color(Color.LIME, Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	chainshot = new Effect(9f, e -> {
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Lines.stroke(e.fract()*4f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*7f);
		Lines.stroke(e.fract()*2f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*10f);
		Draw.reset();
	}),
	
	mortarshot = new Effect(10f, e -> {
		Draw.color(Color.WHITE, Color.DARK_GRAY, e.ifract());
		Lines.stroke(e.fract()*6f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*10f);
		Lines.stroke(e.fract()*5f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*14f);
		Lines.stroke(e.fract()*1f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*16f);
		Draw.reset();
	}),
	
	railshot = new Effect(9f, e -> {
		Draw.color(Color.WHITE, Color.DARK_GRAY, e.ifract());
		Lines.stroke(e.fract()*5f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*8f);
		Lines.stroke(e.fract()*4f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*12f);
		Lines.stroke(e.fract()*1f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*14f);
		Draw.reset();
	}),
	
	titanshot = new Effect(12f, e -> {
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Lines.stroke(e.fract()*7f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*12f);
		Lines.stroke(e.fract()*4f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*16f);
		Lines.stroke(e.fract()*2f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*18f);
		Draw.reset();
	}),
	
	largeCannonShot = new Effect(11f, e -> {
		Draw.color(Color.WHITE, whiteYellow, e.ifract());
		Lines.stroke(e.fract()*6f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*12f);
		Lines.stroke(e.fract()*3f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*16f);
		Lines.stroke(e.fract()*1f);
		Lines.lineAngle(e.x, e.y, e.rotation, e.fract()*18f);
		Draw.reset();
	}),
	
	shockwave = new Effect(10f, 80f, e -> {
		Draw.color(Color.WHITE, Color.LIGHT_GRAY, e.ifract());
		Lines.stroke(e.fract()*2f + 0.2f);
		Lines.circle(e.x, e.y, e.ifract()*28f);
		Draw.reset();
	}),
	
	nuclearShockwave = new Effect(10f, 200f, e -> {
		Draw.color(Color.WHITE, Color.LIGHT_GRAY, e.ifract());
		Lines.stroke(e.fract()*3f + 0.2f);
		Lines.poly(e.x, e.y, 40, e.ifract()*140f);
		Draw.reset();
	}),
	
	shockwaveSmall = new Effect(10f, e -> {
		Draw.color(Color.WHITE, Color.LIGHT_GRAY, e.ifract());
		Lines.stroke(e.fract()*2f + 0.1f);
		Lines.circle(e.x, e.y, e.ifract()*15f);
		Draw.reset();
	}),
	
	empshockwave = new Effect(7f, e -> {
		Draw.color(Color.WHITE, Color.SKY, e.ifract());
		Lines.stroke(e.fract()*2f);
		Lines.circle(e.x, e.y, e.ifract()*40f);
		Draw.reset();
	}),
	
	empspark = new Effect(13, e -> {
		Angles.randLenVectors(e.id, 7, 1f + e.ifract()*12f, (x, y)->{
			float len = 1f+e.fract()*6f;
			Draw.color(Color.SKY);
			Lines.lineAngle(e.x + x, e.y + y, Mathf.atan2(x, y), len);
			Draw.reset();
		});
	}),
	
	redgeneratespark = new Effect(18, e -> {
		Angles.randLenVectors(e.id, 5, e.ifract()*8f, (x, y)->{
			float len = e.fract()*4f;
			Draw.color(Color.valueOf("fbb97f"), Color.GRAY, e.ifract());
			//Draw.alpha(e.fract());
			Draw.rect("circle", e.x + x, e.y + y, len, len);
			Draw.reset();
		});
	}),
	
	generatespark = new Effect(18, e -> {
		Angles.randLenVectors(e.id, 5, e.ifract()*8f, (x, y)->{
			float len = e.fract()*4f;
			Draw.color(Color.valueOf("d2b29c"), Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, len, len);
			Draw.reset();
		});
	}),

	fuelburn = new Effect(23, e -> {
		Angles.randLenVectors(e.id, 5, e.ifract()*9f, (x, y)->{
			float len = e.fract()*4f;
			Draw.color(Color.LIGHT_GRAY, Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, len, len);
			Draw.reset();
		});
	}),

	pulverize = new Effect(25, e -> {
		Angles.randLenVectors(e.id, 5, 3f + e.ifract()*5f, (x, y)->{
			Draw.color(Color.valueOf("eae4f0"), Color.GRAY, e.ifract());
			Fill.poly(e.x + x, e.y + y, 4, e.fract() * 2f + 0.5f, 45);
			Draw.reset();
		});
	}),
	
	laserspark = new Effect(14, e -> {
		Angles.randLenVectors(e.id, 8, 1f + e.ifract()*11f, (x, y)->{
			float len = 1f+e.fract()*5f;
			Draw.color(Color.WHITE, Color.CORAL, e.ifract());
			Draw.alpha(e.ifract()/1.3f);
			Lines.lineAngle(e.x + x, e.y + y, Mathf.atan2(x, y), len);
			Draw.reset();
		});
	}),
	
	shellsmoke = new Effect(20, e -> {
		Angles.randLenVectors(e.id, 8, 3f + e.ifract()*17f, (x, y)->{
			float size = 2f+e.fract()*5f;
			Draw.color(Color.LIGHT_GRAY, Color.DARK_GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),

	producesmoke = new Effect(12, e -> {
		Angles.randLenVectors(e.id, 8, 4f + e.ifract()*18f, (x, y)->{
			Draw.color(Color.WHITE, Colors.get("accent"), e.ifract());
			Fill.poly(e.x + x, e.y + y, 4, 1f+e.fract()*3f, 45);
			Draw.reset();
		});
	}),
	
	blastsmoke = new Effect(26, e -> {
		Angles.randLenVectors(e.id, 12, 1f + e.ifract()*23f, (x, y)->{
			float size = 2f+e.fract()*6f;
			Draw.color(Color.LIGHT_GRAY, Color.DARK_GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	lava = new Effect(18, e -> {
		Angles.randLenVectors(e.id, 3, 1f + e.ifract()*10f, (x, y)->{
			float size = e.sfract()*4f;
			Draw.color(Color.ORANGE, Color.GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
	}),
	
	lavabubble = new Effect(45f, e -> {
		Draw.color(Color.ORANGE);
		float scl = 0.35f;
		Lines.stroke(1f - Mathf.clamp(e.ifract() - (1f-scl)) * (1f/scl));
		Lines.circle(e.x, e.y, e.ifract()*4f);
		Draw.reset();
	}),
	
	oilbubble = new Effect(64f, e -> {
		Draw.color(Color.DARK_GRAY);
		float scl = 0.25f;
		Lines.stroke(1f - Mathf.clamp(e.ifract() - (1f-scl)) * (1f/scl));
		Lines.circle(e.x, e.y, e.ifract()*3f);
		Draw.reset();
	}),
	
	shellexplosion = new Effect(9, e -> {
		Lines.stroke(2f - e.ifract()*1.7f);
		Draw.color(Color.WHITE, Color.LIGHT_GRAY, e.ifract());
		Lines.circle(e.x, e.y, 3f + e.ifract() * 9f);
		Draw.reset();
	}),
	
	blastexplosion = new Effect(14, e -> {
		Lines.stroke(1.2f - e.ifract());
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Lines.circle(e.x, e.y, 1.5f + e.ifract() * 9f);
		Draw.reset();
	}),
	
	place = new Effect(16, e -> {
		Lines.stroke(3f - e.ifract() * 2f);
		Lines.square(e.x, e.y, tilesize / 2f + e.ifract() * 3f);
		Draw.reset();
	}),
	
	dooropen = new Effect(10, e -> {
		Lines.stroke(e.fract() * 1.6f);
		Lines.square(e.x, e.y, tilesize / 2f + e.ifract() * 2f);
		Draw.reset();
	}),
	
	doorclose= new Effect(10, e -> {
		Lines.stroke(e.fract() * 1.6f);
		Lines.square(e.x, e.y, tilesize / 2f + e.fract() * 2f);
		Draw.reset();
	}),
	
	dooropenlarge = new Effect(10, e -> {
		Lines.stroke(e.fract() * 1.6f);
		Lines.square(e.x, e.y, tilesize + e.ifract() * 2f);
		Draw.reset();
	}),
			
	doorcloselarge = new Effect(10, e -> {
		Lines.stroke(e.fract() * 1.6f);
		Lines.square(e.x, e.y, tilesize + e.fract() * 2f);
		Draw.reset();
	}),
	
	purify = new Effect(10, e -> {
		Draw.color(Color.ROYAL, Color.GRAY, e.ifract());
		Lines.stroke(2f);
		Lines.spikes(e.x, e.y, e.ifract() * 4f, 2, 6);
		Draw.reset();
	}),
	
	purifyoil = new Effect(10, e -> {
		Draw.color(Color.BLACK, Color.GRAY, e.ifract());
		Lines.stroke(2f);
		Lines.spikes(e.x, e.y, e.ifract() * 4f, 2, 6);
		Draw.reset();
	}),
	
	purifystone = new Effect(10, e -> {
		Draw.color(Color.ORANGE, Color.GRAY, e.ifract());
		Lines.stroke(2f);
		Lines.spikes(e.x, e.y, e.ifract() * 4f, 2, 6);
		Draw.reset();
	}),
	
	generate = new Effect(11, e -> {
		Draw.color(Color.ORANGE, Color.YELLOW, e.ifract());
		Lines.stroke(1f);
		Lines.spikes(e.x, e.y, e.ifract() * 5f, 2, 8);
		Draw.reset();
	}),

	spark = new Effect(10, e -> {
		Lines.stroke(1f);
		Draw.color(Color.WHITE, Color.GRAY, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 5f, 2, 8);
		Draw.reset();
	}),
	
	sparkbig = new Effect(11, e -> {
		Lines.stroke(1f);
		Draw.color(lightRed, Color.GRAY, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 5f, 2.3f, 8);
		Draw.reset();
	}),
	
	smelt = new Effect(10, e -> {
		Lines.stroke(1f);
		Draw.color(Color.YELLOW, Color.RED, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 5f, 1f, 8);
		Draw.reset();
	}),

	breakBlock = new Effect(12, e -> {
		Lines.stroke(2f);
		Draw.color(Color.WHITE, Colors.get("break"), e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 6f, 2, 5, 90);
		Draw.reset();
	}),

	hit = new Effect(10, e -> {
		Lines.stroke(1f);
		Draw.color(Color.WHITE, Color.ORANGE, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 3f, 2, 8);
		Draw.reset();
	}),
	
	laserhit = new Effect(10, e -> {
		Lines.stroke(1f);
		Draw.color(Color.WHITE, Color.SKY, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 2f, 2, 6);
		Draw.reset();
	}),
	
	shieldhit = new Effect(9, e -> {
		Lines.stroke(1f);
		Draw.color(Color.WHITE, Color.SKY, e.ifract());
		Lines.spikes(e.x, e.y, e.ifract() * 5f, 2, 6);
		Lines.stroke(4f*e.fract());
		Lines.circle(e.x, e.y, e.ifract()*14f);
		Draw.reset();
	}),
	
	laserShoot = new Effect(8, e -> {
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation, 3, e.fract(), 6f, 2f, 0.8f);
		Draw.reset();
	}),

	spreadShoot = new Effect(12, e -> {
		Draw.color(Color.WHITE, Color.PURPLE, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation, 3, e.fract(), 9f, 3.5f, 0.8f);
		Draw.reset();
	}),

	clusterShoot = new Effect(12, e -> {
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation, 3, e.fract(), 10f, 2.5f, 0.7f);
		Draw.reset();
	}),
	
	vulcanShoot = new Effect(8, e -> {
		Draw.color(lighterOrange, lightOrange, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation, 3, e.fract(), 10f, 2f, 0.7f);
		Draw.reset();
	}),
	
	shockShoot = new Effect(8, e -> {
		Draw.color(Color.WHITE, Color.ORANGE, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation, 3, e.fract(), 14f, 4f, 0.8f);
		Draw.reset();
	}),

	beamShoot = new Effect(8, e -> {
		Draw.color(beamLight, beam, e.ifract());
		Shapes.lineShot(e.x, e.y, e.rotation - 70, 3, e.fract(), 12f, 1f, 0.5f);
		Shapes.lineShot(e.x, e.y, e.rotation + 70, 3, e.fract(), 12f, 1f, 0.5f);
		Draw.reset();
	}),

    beamhit = new Effect(8, e -> {
        Draw.color(beamLight, beam, e.ifract());
        Lines.stroke(e.fract()*3f+0.5f);
        Lines.circle(e.x, e.y, e.ifract()*8f);
        Lines.spikes(e.x, e.y, e.ifract()*6f, 2f, 4, 45);
        Draw.reset();
    }),

	explosion = new Effect(11, e -> {
		Lines.stroke(2f*e.fract()+0.5f);
		Draw.color(Color.WHITE, Color.DARK_GRAY, e.powfract());
		Lines.circle(e.x, e.y, 5f + e.powfract() * 6f);
		
		Draw.color(e.ifract() < 0.5f ? Color.WHITE : Color.DARK_GRAY);
		Angles.randLenVectors(e.id, 5, 8f, (x, y)->{
			Fill.circle(e.x + x, e.y + y, e.fract()*5f + 2.5f);
		});
		
		Draw.reset();
	}),
	
	
	blockexplosion = new Effect(13, e -> {
		Angles.randLenVectors(e.id+1, 8, 5f + e.ifract()*11f, (x, y)->{
			float size = 2f+e.fract()*8f;
			Draw.color(Color.LIGHT_GRAY, Color.DARK_GRAY, e.ifract());
			Draw.rect("circle", e.x + x, e.y + y, size, size);
			Draw.reset();
		});
		
		Lines.stroke(2f*e.fract()+0.4f);
		Draw.color(Color.WHITE, Color.ORANGE, e.powfract());
		Lines.circle(e.x, e.y, 2f + e.powfract() * 9f);
		
		Draw.color(e.ifract() < 0.5f ? Color.WHITE : Color.DARK_GRAY);
		Angles.randLenVectors(e.id, 5, 8f, (x, y)->{
			Fill.circle(e.x + x, e.y + y, e.fract()*5f + 1f);
		});
		
		Draw.reset();
	}),

	clusterbomb = new Effect(10f, e -> {
		Draw.color(Color.WHITE, lightOrange, e.ifract());
		Lines.stroke(e.fract()*1.5f);
		Lines.poly(e.x, e.y, 4, e.fract()*8f);
		Lines.circle(e.x, e.y, e.ifract()*14f);
		Draw.reset();
	}),
	
	coreexplosion = new Effect(13, e -> {
		Lines.stroke(3f-e.ifract()*2f);
		Draw.color(Color.ORANGE, Color.WHITE, e.ifract());
		Lines.spikes(e.x, e.y, 5f + e.ifract() * 40f, 6, 6);
		Lines.circle(e.x, e.y, 4f + e.ifract() * 40f);
		Draw.reset();
	}),
	
	smoke = new Effect(100, e -> {
		Draw.color(Color.GRAY, new Color(0.3f, 0.3f, 0.3f, 1f), e.ifract());
		float size = 7f-e.ifract()*7f;
		Draw.rect("circle", e.x, e.y, size, size);
		Draw.reset();
	}),
	
	railsmoke = new Effect(30, e -> {
		Draw.color(Color.LIGHT_GRAY, Color.WHITE, e.ifract());
		float size = e.fract()*4f;
		Draw.rect("circle", e.x, e.y, size, size);
		Draw.reset();
	}),

	chainsmoke = new Effect(30, e -> {
		Draw.color(lightGray);
		float size = e.fract()*4f;
		Draw.rect("circle", e.x, e.y, size, size);
		Draw.reset();
	}),
	
	dashsmoke = new Effect(30, e -> {
		Draw.color(Color.CORAL, Color.GRAY, e.ifract());
		float size = e.fract()*4f;
		Draw.rect("circle", e.x, e.y, size, size);
		Draw.reset();
	}),
	
	spawn = new Effect(23, e -> {
		Lines.stroke(2f);
		Draw.color(Color.DARK_GRAY, Color.SCARLET, e.ifract());
		Lines.circle(e.x, e.y, 7f - e.ifract() * 6f);
		Draw.reset();
	}),
	
	respawn = new Effect(respawnduration, e -> {
		Draw.tcolor(Color.SCARLET);
		Draw.tscl(0.25f);
		Draw.text("Respawning in " + (int)((e.lifetime-e.time)/60), e.x, e.y);
		Draw.tscl(0.5f);
		Draw.reset();
	}),
	transfer = new Effect(20, e -> {
		Draw.color(Color.SCARLET, Color.CLEAR, e.fract());
		Lines.square(e.x, e.y, 4);
		Lines.lineAngle(e.x, e.y, e.rotation, 5f);
		Draw.reset();
	});
}
