package main;

import controlP5.*;
import org.philhosoft.p8g.svg.P8gGraphicsSVG;
import processing.core.PApplet;
import processing.core.PFont;
import toxi.geom.Vec2D;
import toxi.processing.ToxiclibsSupport;
import util.Color;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.text.DecimalFormat;

public class App extends PApplet {
	public static final DecimalFormat DF3 = new DecimalFormat("#.###");
	public static PApplet P5;
	public static String xmlFilePath = "./data/flowgraph_test_lg.xml";
	public static boolean UPDATE_PHYSICS = true;
	public static boolean UPDATE_VALUES = true;
	//	public static boolean UPDATE_VORONOI = true;
	public static boolean UPDATE_FLOWGRAPH = true;
	public static boolean isShiftDown;
	public static float ZOOM = 1;
	public static float world_scale = 10;
	public static ToxiclibsSupport GFX;
	//	public static Vec2D MOUSE = new Vec2D();
	public static PSys PSYS;
	public static FSys FSYS;
	public static VSys VSYS;
	public Group properties, generator, config;
	public static Knob radiusSlider, colorSlider, capacitySlider;
	public static Textfield nameTextfield;
	private PFont pfont;
	private Accordion accordion;
	public boolean RECORDING = false;
	public ControlP5 CP5;
	public static void main(String[] args) { PApplet.main(new String[]{("main.App")}); System.out.println(System.getProperty("user.dir")); }
	public static void __rebelReload() { System.out.println("__rebelReload");/* initGUI();*/}
	private void initGUI() {
		CP5.enableShortcuts();
		CP5.setAutoDraw(false);
		CP5.setFont(pfont, 10);
		CP5.setAutoSpacing(4, 8);
		CP5.setColorBackground(Color.CP5_BG).setColorForeground(Color.CP5_FG).setColorActive(Color.CP5_ACT);
		CP5.setColorCaptionLabel(Color.CP5_CAP).setColorValueLabel(Color.CP5_VAL);
		initGUI_settings();
		initGUI_menu();
		initGUI_object();
		initGUI_styles();
		accordion = CP5.addAccordion("acc").setPosition(0, 92).setWidth(220).setCollapseMode(Accordion.MULTI);
		accordion.addItem(config).addItem(generator).addItem(properties);
		accordion.open(0, 1);
	}
	private void initGUI_menu() {
		MultiList mainMenu = CP5.addMultiList("myList", 90, 0, 130, 24);
		MultiListButton file;
		file = mainMenu.add("File", 1); file.setWidth(130); file.setHeight(20);
		file.add("file_quit", 11).setCaptionLabel("Quit");
		file.add("file_open", 12).setCaptionLabel("Open XML");
		file.add("file_save", 13).setCaptionLabel("Save XML");
		file.add("file_print", 14).setCaptionLabel("Print SVG");
		file.add("file_loadDef", 15).setCaptionLabel("Load Defaults");
		file.add("file_saveDef", 16).setCaptionLabel("Save Defaults");
		MultiListButton view;
		view = mainMenu.add("View", 2); view.setWidth(130); view.setHeight(20);
		view.add("view_nodes", 21).setCaptionLabel("Nodes");
		view.add("view_nodesColor", 28).setCaptionLabel("Nodes Color");
		view.add("view_relations", 25).setCaptionLabel("Relations");
		view.add("view_outliner", 25).setCaptionLabel("Outliner");
		view.add("view_particles", 22).setCaptionLabel("Verlet Particles");
		view.add("view_springs", 23).setCaptionLabel("Verlet Springs");
		view.add("view_minDist", 23).setCaptionLabel("Verlet MinDist");
		view.add("view_weights", 23).setCaptionLabel("Verlet Weights");
		view.add("view_behaviors", 23).setCaptionLabel("Verlet Behaviors");
		view.add("view_physInfo", 26).setCaptionLabel("Verlet Info");
		view.add("view_voronoi", 24).setCaptionLabel("Show Voronoi");
		view.add("view_vorInfo", 27).setCaptionLabel("Show Vor Info");
		MultiListButton run;
		run = mainMenu.add("Run", 3); run.setWidth(130); run.setHeight(20);
		run.add("run_physics", 31).setCaptionLabel("Run Physics");
		run.add("run_voronoi", 32).setCaptionLabel("Run Voronoi");
		run.add("run_updateVals", 33).setCaptionLabel("Update Values");
		run.add("run_flowgraph", 33).setCaptionLabel("Update FlowGraph");
		MultiListButton edit;
		edit = mainMenu.add("Edit", 4); edit.setWidth(130); edit.setHeight(20);
		edit.add("edit_addMinDist", 41).setCaptionLabel("Add MinDist");
		edit.add("edit_rebuildMinD", 42).setCaptionLabel("Rebuild MinDist");
		edit.add("edit_clearMinD", 43).setCaptionLabel("Clear MinDist");
		edit.add("edit_clearSpr", 44).setCaptionLabel("Clear Springs");
		edit.add("edit_clearAll", 45).setCaptionLabel("Clear All");
	}
	private void initGUI_object() {
		CP5.begin(0, 0);
		properties = CP5.addGroup("OBJECT_PROPERTIES").setBackgroundHeight(200).setBarHeight(32).setWidth(220);
		radiusSlider = CP5.addKnob("setSize").setCaptionLabel("Size").setRange(0, 500).setPosition(10, 30).setDecimalPrecision(1);
		radiusSlider.setValue(50);
		radiusSlider.addListener(new radiusSliderListener());
		radiusSlider.hide();
		colorSlider = CP5.addKnob("setColor").setCaptionLabel("Color").setRange(0, 360).setPosition(80, 30).setDecimalPrecision(0);
		colorSlider.setValue(180);
		colorSlider.addListener(new colorSliderListener());
		colorSlider.hide();
		capacitySlider = CP5.addKnob("setCapacity").setCaptionLabel("Capacity").setRange(1, 200).setPosition(150, 30).setDecimalPrecision(0);
		capacitySlider.setValue(1);
		capacitySlider.addListener(new capacitySliderListener());
		capacitySlider.hide();
		nameTextfield = CP5.addTextfield("setName").setCaptionLabel("Unique Datablock ID Name").setPosition(40, 140);
		nameTextfield.setValue("untitled");
		nameTextfield.addListener(new nameTextfieldListener());
		nameTextfield.hide();
		CP5.end();
	}
	private void initGUI_settings() {
		config = CP5.addGroup("VERLET PHYSICS SETTINGS").setBackgroundHeight(260).setBarHeight(32).setWidth(220);
		CP5.begin(10, 15);
		CP5.addSlider("world_scale").setRange(1, 20).setDecimalPrecision(0).linebreak();
		CP5.addSlider("verlet_drag").setValue(PSYS.getDrag()).setRange(0.1f, 1).setDecimalPrecision(2).linebreak();
		CP5.addSlider("particle_scale").setValue(PSYS.particleScale).setRange(0.5f, 2).setDecimalPrecision(1).linebreak();
		CP5.addSlider("particle_weight").setValue(PSYS.particleWeight).setRange(0.1f, 2).setDecimalPrecision(1).linebreak();
		CP5.addSlider("particle_padding").setValue(PSYS.particlePadding).setRange(0.01f, 10).setDecimalPrecision(0).linebreak();
		CP5.addSlider("behavior_scale").setValue(PSYS.behaviorScale).setRange(1, 2).setDecimalPrecision(1).linebreak();
		CP5.addSlider("behavior_strength").setValue(PSYS.behaviorStrength).setRange(-1f, 1).setDecimalPrecision(2).linebreak();
		CP5.addSlider("spring_scale").setValue(PSys.springScale).setRange(0.5f, 2).setDecimalPrecision(1).linebreak();
		CP5.addSlider("spring_strength").setValue(PSys.springStrength).setRange(0.001f, 0.05f).setDecimalPrecision(3).linebreak();
		CP5.end();
		generator = CP5.addGroup("RECURSIVE GRAPH GENERATOR").setBackgroundHeight(140).setBarHeight(32).setWidth(220);
		CP5.begin(10, 10);
		CP5.addNumberbox("ITER_A").setPosition(10, 14).linebreak();
		CP5.addNumberbox("ITER_B").setPosition(10, 38).linebreak();
		CP5.addNumberbox("ITER_C").setPosition(10, 62).linebreak();
		CP5.addNumberbox("ITER_D").setPosition(10, 86).linebreak();
		CP5.addNumberbox("ITER_E").setPosition(10, 110).linebreak();
		CP5.end();
	}
	private void initGUI_styles() {
		for (Button b : CP5.getAll(Button.class)) {
			b.setSize(130, 22);
			b.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setFont(pfont);
		} for (Numberbox n : CP5.getAll(Numberbox.class)) {
			n.setSize(200, 16).setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0);
			n.setGroup(generator);
			n.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER);
			n.getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER);
		} for (Knob k : CP5.getAll(Knob.class)) {
			k.setRadius(30);
			k.setDragDirection(Knob.HORIZONTAL);
			k.setGroup(properties);
		} for (Textfield t : CP5.getAll(Textfield.class)) {
			t.setSize(140, 22);
			t.setAutoClear(false);
			t.getCaptionLabel().align(ControlP5.CENTER, ControlP5.BOTTOM_OUTSIDE).getStyle().setPaddingTop(4);
			t.setGroup(properties);
		} for (Slider s : CP5.getAll(Slider.class)) {
			s.setSize(170, 16).showTickMarks(false).setHandleSize(12).setSliderMode(Slider.FLEXIBLE);
			s.getValueLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
			s.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).getStyle().setPaddingRight(4);
			s.setGroup(config);
		} for (Group g : CP5.getAll(Group.class)) {
			g.setBackgroundColor(Color.CP5_GRP);
			g.getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		}
	}
	public void setup() {
		P5 = this;
		size(1600, 1000, P2D);
		frameRate(60);
		smooth(8);
		colorMode(HSB, 360, 100, 100, 100);
		background(Color.BG);
		ellipseMode(RADIUS);
		textAlign(LEFT);
		textSize(10);
		strokeWeight(1);
		noStroke();
		noFill();
		pfont = createFont("SourceCodePro", 10);
		GFX = new ToxiclibsSupport(this);
		CP5 = new ControlP5(this);
		PSYS = new PSys();
		FSYS = new FSys();
		VSYS = new VSys();
		initGUI();
	}
	public void draw() {
		background(Color.BG);
		if (RECORDING) { RECORDING = false; endRecord(); System.out.println("SVG EXPORTED SUCCESSFULLY"); }
		pushMatrix();
		translate(-((ZOOM * width) - width) / 2, -((ZOOM * height) - height) / 2);
		scale(ZOOM);
		VSYS.draw();
		PSYS.draw();
		FSYS.draw();
		popMatrix();
		draw_GUI();
	}
	private void draw_GUI() {
		fill(Color.CP5_BG);
		rect(0, 0, 89, 91);
		noFill();
		CP5.draw();
	}

	public void mouseMoved() { }
	public void mousePressed() {
		if (mouseButton == RIGHT) {
			FSYS.selectNodeNearPosition(mousePos());

			toggleObjProperties();
		}
	}
	private void toggleObjProperties() {

		if (FSYS.hasActiveNode()) {
			radiusSlider.setValue(FSYS.getActiveNode().size);
			colorSlider.setValue(FSYS.getActiveNode().color);
			capacitySlider.setValue(FSYS.getActiveNode().capacity);
			nameTextfield.setValue(FSYS.getActiveNode().name);
			radiusSlider.show();
			colorSlider.show();
			capacitySlider.show();
			nameTextfield.show();
			accordion.open(2);
		} else {
			radiusSlider.hide();
			colorSlider.hide();
			capacitySlider.hide();
			nameTextfield.hide();
			accordion.close(2);
		}
	}
	public void mouseDragged() {
		if (mouseButton == RIGHT) {
			if (FSYS.hasActiveNode()) { FSYS.moveActiveNode(mousePos()); }
		}
	}
	public void mouseReleased() {}
	public void keyPressed() {
		if (key == CODED && keyCode == SHIFT) { isShiftDown = true; }
		switch (key) {
			case '1': System.out.println("1 : "); VSYS.showPolygons = !VSYS.showPolygons; break;
			case '2': System.out.println("2 : "); VSYS.showBezier = !VSYS.showBezier; break;
			case '3': System.out.println("3 : "); VSYS.showVerts = !VSYS.showVerts; break;
			case '4': System.out.println("4 : "); VSYS.showInfo = !VSYS.showInfo; break;
			case 'c': VSYS.doClip = !VSYS.doClip; break;
			case 'a': System.out.println("A : createNode()"); FSYS.createNode(mousePos()); toggleObjProperties(); break;
			case 'f': System.out.println("F : createRelation()"); FSYS.createRelation(); break;
			case 'q': System.out.println("Q : divideNode()"); FSYS.divideNode(); break;
			case 'w': System.out.println("W : multiplyNode()"); FSYS.multiplyNode(); break;
			case 'l': System.out.println("L : freezeNode()"); FSYS.freezeNode(); break;
		}
	}
	public Vec2D mousePos() {return new Vec2D(mouseX, mouseY);}
	public void keyReleased() { if (key == CODED && keyCode == SHIFT) { isShiftDown = false; } }
	public void controlEvent(ControlEvent theEvent) {
		if (!theEvent.isGroup()) {
//			System.out.println("Controller: " + theEvent.getController().getName() + "Value: " + theEvent.getController().getValue());
			switch (theEvent.getController().getName()) {
				case "file_quit": System.out.println("[quit]"); exit(); break;
				case "file_open": unmarshal(); break;
				case "file_save": marshal(); break;
				case "file_print": beginRecord(P8gGraphicsSVG.SVG, "./out/svg/print-###.svg"); RECORDING = true; break;
				case "file_loadDef": CP5.loadProperties((xmlFilePath + "defaults.ser")); break;
				case "file_saveDef": CP5.saveProperties((xmlFilePath + "defaults.ser")); break;

				case "view_nodes": FSys.showNodes = !FSys.showNodes; break;
				case "view_nodesColor": FSys.showNodesCol = !FSys.showNodesCol; break;
				case "view_relations": FSys.showRelations = !FSys.showRelations; break;
				case "view_outliner": FSys.showOutliner = !FSys.showOutliner; break;
				case "view_particles": PSYS.showParticles = !PSYS.showParticles; break;
				case "view_springs": PSYS.showSprings = !PSYS.showSprings; break;
				case "view_minDist": PSYS.showMinDist = !PSYS.showMinDist; break;
				case "view_weights": PSYS.showWeights = !PSYS.showWeights; break;
				case "view_behaviors": PSYS.showBehaviors = !PSYS.showBehaviors; break;
				case "view_physInfo": PSYS.showInfo = !PSYS.showInfo; break;
				case "view_voronoi": VSYS.showVoronoi = !VSYS.showVoronoi; break;
				case "view_vorInfo": VSYS.showInfo = !VSYS.showInfo; break;

				case "run_physics": PSYS.isUpdating = !PSYS.isUpdating; break;
				case "run_flowgraph": FSYS.isUpdating = !FSYS.isUpdating; break;
				case "run_voronoi": VSYS.isUpdating = !VSYS.isUpdating; break;

				case "edit_addMinDist": PSYS.addMinDist(); break;
				case "edit_rebuildMinD": PSYS.clearMinDist(); PSYS.addMinDist(); break;
				case "edit_clearMinD": PSYS.clearMinDist(); break;
				case "edit_clearSpr": PSYS.clearSprings(); break;
				case "edit_clearAll": PSYS.clearAll(); FSYS.clear(); break;

				case "world_scale": world_scale = theEvent.getController().getValue(); break;
				case "verlet_drag": PSYS.setDrag(theEvent.getController().getValue()); break;
				case "particle_scale": PSYS.particleScale = theEvent.getController().getValue(); break;
				case "particle_padding": PSYS.particlePadding = theEvent.getController().getValue(); break;
				case "particle_weight": PSYS.particleWeight = theEvent.getController().getValue(); break;
				case "behavior_scale": PSYS.behaviorScale = theEvent.getController().getValue(); break;
				case "behavior_strength": PSYS.behaviorStrength = theEvent.getController().getValue(); break;
				case "spring_scale": PSys.springScale = theEvent.getController().getValue(); break;
				case "spring_strength": PSys.springStrength = theEvent.getController().getValue(); break;
			}
		}
	}
	private void unmarshal() {
		System.out.println("Unmarshal-----------------------------------");
		try {
			JAXBContext context = JAXBContext.newInstance(FSys.class);
			FSYS = (FSys) context.createUnmarshaller().unmarshal(createInput(xmlFilePath));
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(FSYS, System.out);
		} catch (JAXBException e) { System.out.println("error parsing xml: "); e.printStackTrace(); System.exit(1); }
		FSYS.build();
	}
	private void marshal() {
		System.out.println("Marshal-------------------------------------");
		try {
			JAXBContext context = JAXBContext.newInstance(FSys.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(FSYS, System.out);
			m.marshal(FSYS, new File(xmlFilePath));
		} catch (JAXBException e) { System.out.println("error parsing xml: "); e.printStackTrace(); System.exit(1); }
		FSYS.build();
	}

	static class nameTextfieldListener implements ControlListener {
		public void controlEvent(ControlEvent e) {
			String name = e.getController().getStringValue();
			FSYS.getActiveNode().setName(name);
			if (e.getController().isMousePressed()) {
				for (FSys.Node n : FSYS.getSelectedNodes()) { n.setName(name); }
			}
		}
	}

	static class colorSliderListener implements ControlListener {
		int color;
		public void controlEvent(ControlEvent e) {
			color = (int) e.getController().getValue();
			FSYS.getActiveNode().setColor(color);
			if (e.getController().isMousePressed()) {
				for (FSys.Node n : FSYS.getSelectedNodes()) { n.setColor(color); }
			}
		}
	}

	static class capacitySliderListener implements ControlListener {
		int capacity;
		public void controlEvent(ControlEvent e) {
			capacity = (int) e.getController().getValue();
			FSYS.getActiveNode().setCapacity(capacity);
			if (e.getController().isMousePressed()) {
				for (FSys.Node n : FSYS.getSelectedNodes()) { n.setCapacity(capacity); }
			}
		}
	}

	static class radiusSliderListener implements ControlListener {
		float size;
		public void controlEvent(ControlEvent e) {
			size = e.getController().getValue();
			FSYS.getActiveNode().setSize(e.getController().getValue());
			if (e.getController().isMousePressed()) {
				for (FSys.Node n : FSYS.getSelectedNodes()) {
					n.setSize(size);
				}
			}/*FSYS.update();*/
		}
	}
}
