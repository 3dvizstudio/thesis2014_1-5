package main;

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Rect;
import toxi.physics2d.VerletMinDistanceSpring2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.VerletSpring2D;
import toxi.physics2d.behaviors.AttractionBehavior2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;
import toxi.processing.ToxiclibsSupport;
import util.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PSys {
	private final VerletPhysics2D physics;
	private final Rect bounds;
	private List<VerletSpring2D> springs;
	private List<VerletSpring2D> minDistSprings;
	private List<AttractionBehavior2D> behaviors;
	private List<VerletParticle2D> particles;
	private HashMap<String, String> info;
	public boolean showParticles = true;
	public boolean showSprings = true;
	public boolean showBehaviors;
	public boolean showWeights;
	public boolean showMinDist;
	public boolean showInfo;
	public boolean isUpdating = true;
	public float particleScale = 1;
	public float particleWeight = 0.5f;
	public float particlePadding = 0.1f;
	public float behaviorScale =1.5f;
	public float behaviorStrength = -0.3f;
	public static float springScale = 1;
	public static float springStrength = 0.001f;

	public PSys() {
		physics = new VerletPhysics2D();
		bounds = new Rect(350, 50, App.P5.width - 550, App.P5.height - 100);
		physics.setWorldBounds(bounds);
		physics.setDrag(0.3f);
		springs = new ArrayList<>();
		behaviors = new ArrayList<>();
		particles = new ArrayList<>();
		minDistSprings = new ArrayList<>();
		info = new HashMap<>();
	}
	private void update() {
		physics.update();
		for (VerletSpring2D s : springs) { s.setStrength(springStrength); }
		for (VerletParticle2D n : particles) { n.setWeight(particleWeight); }
		for (AttractionBehavior2D b : behaviors) {b.setStrength(behaviorStrength);}
		info.put("Springs : ", String.valueOf(physics.springs.size()));
		info.put("Particles : ", String.valueOf(physics.particles.size()));
		info.put("Behaviors : ", String.valueOf(physics.behaviors.size()));
		info.put("Drag : ", App.DF3.format(physics.getDrag()));
		info.put("Iterations : ", App.DF3.format(physics.getNumIterations()));
	}
	public void updateSpring(FSys.Relation r){
		VerletParticle2D a = r.a();
		VerletParticle2D b = r.b();
		float l = r.length();
		physics.getSpring(a,b).setRestLength(l).setStrength(springStrength);
	}
	public void updateParticle(FSys.Node n){
		n.behavior.setRadius(n.radius*behaviorScale);
		n.behavior.setStrength(behaviorStrength);
		n.verlet.setWeight(particleWeight);
	}
	public void draw() {
		ToxiclibsSupport gfx = App.GFX;
		PGraphics pg = gfx.getGraphics();
		if (isUpdating)update();
		pg.pushMatrix();
		pg.stroke(Color.GRID);
		gfx.rect(getBounds());
		if (showSprings) {
			pg.stroke(0, 0, 50);
			for (VerletSpring2D s : springs) { gfx.line(s.a, s.b); }
		}
		if (showMinDist) {
			pg.stroke(0, 0, 50);
			for (VerletSpring2D s : minDistSprings) { gfx.line(s.a, s.b); }
		}
		if (showParticles) {
			pg.stroke(Color.GREY); pg.fill(Color.GREY_DK);
			for (VerletParticle2D a : physics.particles) { gfx.circle(a, 6); }
		}
		if (showWeights) {
			pg.stroke(Color.GREY); pg.fill(Color.GREY_DK);
			for (VerletParticle2D a : physics.particles) { gfx.circle(a, a.getWeight()); }
		}
		if (showBehaviors) {
			pg.stroke(0xff343434); pg.noFill();
			for (ParticleBehavior2D b : physics.behaviors) {
				AttractionBehavior2D ab = (AttractionBehavior2D) b;
				gfx.circle(ab.getAttractor(), ab.getRadius());
			}
		}
		if (showInfo) {
			pg.fill(0xff666666);
			pg.pushMatrix(); pg.translate(pg.width - 80, pg.height - 150);
			for (Map.Entry entry : info.entrySet()) {
				pg.translate(0, 10);
				pg.textAlign(PConstants.LEFT); pg.text(String.valueOf(entry.getKey()), -50, 0);
				pg.textAlign(PConstants.RIGHT); pg.text(String.valueOf(entry.getValue()), 50, 0);
			} pg.popMatrix();
		}
		pg.noStroke(); pg.noFill();
		pg.popMatrix();
	}
	public void addMinDist() {
		FSys FSYS = App.FSYS;
		for (FSys.Node na : FSYS.getNodes()) {
			for (FSys.Node nb : FSYS.getNodes()) {
				if (na != nb) {
					float len = (na.getRadius() + nb.getRadius());
					VerletParticle2D va = na.verlet;
					VerletParticle2D vb = nb.verlet;
					VerletSpring2D s = new VerletMinDistanceSpring2D(va, vb, len, .01f);
					minDistSprings.add(s);
					physics.addSpring(s);
				}
			}
		}
	}
	public void addParticle(FSys.Node n) {
		particles.add(n.verlet);
		physics.addParticle(n.verlet);
		behaviors.add(n.behavior);
		physics.addBehavior(n.behavior);
	}
	public void addSpring(FSys.Relation r) {
		VerletParticle2D a = r.a();
		VerletParticle2D b = r.b();
		float l = r.length();
		VerletSpring2D s = new VerletSpring2D(a, b, l, springStrength);
		springs.add(s);
		physics.addSpring(s);
	}
	public void addSpring(FSys.Node na, FSys.Node nb) {
		float l = na.getRadius() + nb.getRadius() + 5;
		VerletSpring2D s = new VerletSpring2D(na.verlet, nb.verlet, l, 0.01f);
		physics.addSpring(s);
		springs.add(s);
	}
	public void clearMinDist() { for (VerletSpring2D s : minDistSprings) { physics.springs.remove(s); } }
	public void clearSprings() { physics.springs.clear(); }
	public void clearAll() {springs.clear(); minDistSprings.clear(); physics.clear(); }
	public VerletPhysics2D getPhysics() { return physics; }
	public Rect getBounds() { return bounds; }
	public float getDrag() {return physics.getDrag();}
	public void setDrag(float drag) { physics.setDrag(drag);}
}