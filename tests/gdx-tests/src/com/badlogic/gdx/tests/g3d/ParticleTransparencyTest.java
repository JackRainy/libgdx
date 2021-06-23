package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class ParticleTransparencyTest extends BaseG3dTest{
	public static final String DEFAULT_PARTICLE = "data/pre_particle.png",
										DEFAULT_SKIN ="data/uiskin.json";
	Vector3 tmpVector = new Vector3();
	
	private static class SimpleAction extends Action{
		private final ParticleController emitter;
		private float velocity = 3f;
		private final Vector3 position = new Vector3();

		public SimpleAction(ParticleController emitter, Vector3 position) {
			this.emitter = emitter;
			this.position.set(position);
		}

		@Override
		public boolean act (float delta) {
			if (position.y >= 3.0f) velocity = -3.0f; else
			if (position.y <= -3.0f) velocity = 3.0f;
			position.add(0f, velocity * delta, 0f);
			emitter.setTranslation(position);
			return false;
		}
	}
	
	//Simulation
	Array<ParticleController> emitters;
	
	//Rendering
	Model model;
	Environment environment;
	PointSpriteParticleBatch pointSpriteParticleBatch;
	
	//UI
	Stage ui;
	Label fpsLabel;
	StringBuilder builder;
	
	@Override
	public void create () {
		super.create();
		emitters = new Array<>();
		assets.load(DEFAULT_PARTICLE, Texture.class);
		assets.load(DEFAULT_SKIN, Skin.class);
		loading = true;
		showAxes = false;
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0f, 0f, 0.1f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f,  0, -0.5f, -1 ));
		pointSpriteParticleBatch = new PointSpriteParticleBatch();
		pointSpriteParticleBatch.setCamera(cam);
		ui = new Stage();
		builder = new StringBuilder();

		Material material = new Material();
		material.set(new BlendingAttribute(0.5f));

		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(0.25f, 3f, 5f, material,
				VertexAttributes.Usage.Position);
		instances.add(new ModelInstance(model, tmpVector.set(3f, 0f, -2f) ));
		instances.add(new ModelInstance(model, tmpVector.set(-3f, 0f, 2f) ));
	}
	
	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		ui.getViewport().setWorldSize(width, height);
		ui.getViewport().update(width, height, true);
	}
	
	@Override
	protected void onLoaded () {
		Texture particleTexture = assets.get(DEFAULT_PARTICLE);
		pointSpriteParticleBatch.setTexture(assets.get(DEFAULT_PARTICLE, Texture.class));

		addEmitter(new float[] {1f, 0.2f, 0.1f}, particleTexture, tmpVector.set(6f, 0f, 0f));
		addEmitter(new float[] {0.1f, 1f, 0.2f}, particleTexture, tmpVector.set(0f, 1f, -2f));
		addEmitter(new float[] {0.2f, 0.1f, 1f}, particleTexture, tmpVector.set(-6f, 2f, 0f));

		setupUI();
	}
	
	private void addEmitter(float[] colors, Texture particleTexture, Vector3 translation){
		ParticleController controller = createParticleController(colors, particleTexture);
		controller.init();
		controller.start();
		emitters.add(controller);
		controller.translate(translation);
		ui.addAction(new SimpleAction(controller, translation));
	}

	private void setupUI () {
		Skin skin = assets.get(DEFAULT_SKIN);
		Table table = new Table();
		table.setFillParent(true);
		table.top().left().add(new Label("FPS ", skin)).left();
		table.add(fpsLabel = new Label("", skin)).left().expandX().row();
		ui.addActor(table);
	}

	private ParticleController createParticleController(float[] colors, Texture particleTexture) {
		//Emission
		RegularEmitter emitter = new RegularEmitter();
		emitter.getDuration().setLow(300);
		emitter.getEmission().setHigh(290);
		emitter.getLife().setHigh(500);
		emitter.setMaxParticleCount(700);

		//Spawn
		PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();		
		pointSpawnShapeValue.xOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.xOffsetValue.setActive(true);
		pointSpawnShapeValue.yOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.yOffsetValue.setActive(true);
		pointSpawnShapeValue.zOffsetValue.setLow(0, 1f);
		pointSpawnShapeValue.zOffsetValue.setActive(true);
		SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

		//Scale
		ScaleInfluencer scaleInfluencer = new ScaleInfluencer();
		scaleInfluencer.value.setTimeline(new float[]{0, 1});
		scaleInfluencer.value.setScaling(new float[]{1, 0});
		scaleInfluencer.value.setLow(0);
		scaleInfluencer.value.setHigh(1);

		//Color
		ColorInfluencer.Single colorInfluencer = new ColorInfluencer.Single();
		colorInfluencer.colorValue.setColors(new float[] {colors[0], colors[1], colors[2], 0,0,0});
		colorInfluencer.colorValue.setTimeline(new float[] {0, 1});
		colorInfluencer.alphaValue.setHigh(1);
		colorInfluencer.alphaValue.setTimeline(new float[] {0, 0.5f, 0.8f, 1});
		colorInfluencer.alphaValue.setScaling(new float[] {0, 0.15f, 0.5f, 0});
		
		//Dynamics
		DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();
		BrownianAcceleration modifier = new BrownianAcceleration();
		modifier.strengthValue.setTimeline(new float[]{0,1});
		modifier.strengthValue.setScaling(new float[]{0,1});
		modifier.strengthValue.setHigh(40);
		modifier.strengthValue.setLow(1, 5);
		dynamicsInfluencer.velocities.add(modifier);
		
		return new ParticleController("Point Sprite Controller", emitter, new PointSpriteRenderer(pointSpriteParticleBatch),
			new RegionInfluencer.Single(particleTexture),
			spawnSource,
			scaleInfluencer,
			colorInfluencer,
			dynamicsInfluencer
			);
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		if (emitters.size > 0) {
			//Update
			float delta = Gdx.graphics.getDeltaTime();
			builder.delete(0, builder.length());
			builder.append(Gdx.graphics.getFramesPerSecond());
			fpsLabel.setText(builder);
			ui.act(delta);

			pointSpriteParticleBatch.begin();
			for (ParticleController controller : emitters) {
				controller.update();
				controller.draw();
			}
			pointSpriteParticleBatch.end();
			batch.render(pointSpriteParticleBatch, environment);
		}
		batch.render(instances, environment);
		ui.draw();
	}

	@Override
	public void dispose() {
		model.dispose();
		super.dispose();
	}
}
