package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;

import pepse.world.*;

import pepse.world.pepse.world.daynight.Night;
import pepse.world.pepse.world.daynight.Sun;
import pepse.world.pepse.world.daynight.SunHalo;
import pepse.world.pepse.world.daynight.Cloud;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import danogl.collisions.Layer;
import pepse.world.pepse.world.trees.Flora;
import pepse.world.pepse.world.trees.Tree;

/**
 * The PepseGameManager class is responsible for setting up and running the Pepse game.
 * It initializes the game world by creating the sky, terrain, day/night cycle, sun, avatar,
 * energy indicator, clouds, and randomly planted trees.
 */
public class PepseGameManager extends GameManager {

	/**
	 * Initializes the Pepse game by creating all necessary game objects and adding them
	 * to the game’s object collection.
	 *
	 * <p>The initialization steps include:
	 * <ol>
	 *     <li>Creating the Sky background.</li>
	 *     <li>Generating the Terrain (ground blocks) using a random seed.</li>
	 *     <li>Creating the Night overlay for a day/night cycle.</li>
	 *     <li>Spawning the Sun and its Halo.</li>
	 *     <li>Placing the Avatar at the correct height above terrain.</li>
	 *     <li>Adding an EnergyIndicator linked to the Avatar.</li>
	 *     <li>Creating Clouds that interact with the Avatar.</li>
	 *     <li>Randomly planting Trees across the terrain.</li>
	 * </ol>
	 *
	 * @param imageReader      An ImageReader used to load all required textures and sprites.
	 * @param soundReader      A SoundReader used to load sound effects and music.
	 * @param inputListener    A UserInputListener that captures keyboard and mouse events.
	 * @param windowController A WindowController that provides window dimensions and control.
	 */
	@Override
	public void initializeGame(
			ImageReader imageReader,
			SoundReader soundReader,
			UserInputListener inputListener,
			WindowController windowController) {

		super.initializeGame(imageReader, soundReader, inputListener, windowController);

		Vector2 windowDimensions = windowController.getWindowDimensions();
		int initialMinX = 0;
		int initialMaxX = (int) windowDimensions.x();
		// 1. Create the Sky
		GameObject sky = Sky.create(windowDimensions);
		this.gameObjects().addGameObject(sky, Layer.BACKGROUND);

		// 2. Create the ground/terrain
		int seed = new Random().nextInt();
		Terrain terrain = new Terrain(windowDimensions, seed);
		List<Block> groundBlocks = terrain.createInRange(0, (int) windowDimensions.x());
		for (Block block : groundBlocks) {
			this.gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
		}

		// 3. Create Night overlay (30-second cycle)
		float cycleLength = 30f;
		GameObject nightOverlay = Night.create(windowDimensions, cycleLength);
		this.gameObjects().addGameObject(nightOverlay, Layer.FOREGROUND);

		// 4. Create Sun and SunHalo
		GameObject sun = Sun.create(windowDimensions, cycleLength);
		this.gameObjects().addGameObject(sun, Layer.BACKGROUND);

		GameObject sunHalo = SunHalo.create(sun);
		this.gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);

		// 5. Create Avatar at terrain height
		final int LEFT_MARGIN_X = Block.SIZE * 2;
		float groundY = terrain.groundHeightAt(LEFT_MARGIN_X);
		float avatarHeight = Avatar.SIZE;
		Vector2 avatarPos = new Vector2(LEFT_MARGIN_X, groundY - avatarHeight);

		Avatar avatar = new Avatar(avatarPos, inputListener, imageReader);
		avatar.setTag("avatar");
		this.gameObjects().addGameObject(avatar, Layer.DEFAULT);

		// 6. Create EnergyIndicator
		GameObject energyBar = EnergyIndicator.create(avatar);
		this.gameObjects().addGameObject(energyBar, Layer.BACKGROUND);

		// 7. Create Cloud (which will rain when avatar jumps underneath)
		Cloud.create(windowDimensions, this.gameObjects(), avatar);

		// 8. Plant trees randomly across the terrain
		int startX = 0;
		int endX   = (int) windowDimensions.x();

		Flora flora = new Flora(terrain::groundHeightAt, seed);

		/* ---- declare with the new type ---- */
		List<Flora.LayeredObject> floraObjects = flora.createInRange(startX, endX);

		/* ---- add each object to its preferred layer ---- */
		for (Flora.LayeredObject lo : floraObjects) {
			gameObjects().addGameObject(lo.obj(), lo.layer());
		}
		/* (1) build camera exactly as § 9.1 */
		Camera camera = new Camera(
				avatar, Vector2.ZERO,
				windowController.getWindowDimensions(),
				windowController.getWindowDimensions()
		);
		setCamera(camera);

		/* (2) helper lambdas for InfiniteWorldManager */
		float halfW = windowController.getWindowDimensions().x() / 2f;
		Supplier<Float> camLeft  = () -> camera.getCenter().x() - halfW;
		Supplier<Float> camRight = () -> camera.getCenter().x() + halfW;

		BiConsumer<Integer,Integer> onRangeNeeded = (minX, maxX) -> {
			List<Block> ground = terrain.createInRange(minX,maxX);
			ground.forEach(b -> gameObjects().addGameObject(b, Layer.STATIC_OBJECTS));

			flora.createInRange(minX,maxX).forEach(lo ->
					gameObjects().addGameObject(lo.obj(), lo.layer()));
		};

		/* (3) add the manager itself */
		InfiniteWorldManager mgr = new InfiniteWorldManager(
				camLeft, camRight, onRangeNeeded,
				/*initial range we already built:*/ initialMinX, initialMaxX
		);
		gameObjects().addGameObject(mgr, Layer.BACKGROUND);

	}

	/**
	 * The main entry point of the Pepse application. Creates a new instance of the
	 * PepseGameManager and starts the game loop.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main(String[] args) {
		new PepseGameManager().run();
	}
}

// File: Ex4/pepse/PepseGameManager.java
//package pepse;
//
//import danogl.GameManager;
//import danogl.GameObject;
//import danogl.gui.ImageReader;
//import danogl.gui.SoundReader;
//import danogl.gui.UserInputListener;
//import danogl.gui.WindowController;
//import danogl.gui.rendering.Camera;
//import danogl.util.Vector2;
//import danogl.collisions.Layer;
//
//import pepse.world.Block;
//import pepse.world.Terrain;
//import pepse.world.Sky;
//import pepse.world.Avatar;
//import pepse.world.EnergyIndicator;
//import pepse.world.pepse.world.daynight.Cloud;
//import pepse.world.pepse.world.daynight.Night;
//import pepse.world.pepse.world.daynight.Sun;
//import pepse.world.pepse.world.daynight.SunHalo;
//import pepse.world.pepse.world.trees.Flora;
//import pepse.world.pepse.world.trees.Flora.LayeredObject;
//import pepse.world.InfiniteWorldManager;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.BiConsumer;
//import java.util.function.Supplier;
//
//public class PepseGameManager extends GameManager {
//
//	@Override
//	public void initializeGame(
//			ImageReader imageReader,
//			SoundReader soundReader,
//			UserInputListener inputListener,
//			WindowController windowController) {
//		super.initializeGame(imageReader, soundReader, inputListener, windowController);
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 1: Sky background
//		Vector2 windowDimensions = windowController.getWindowDimensions();
//		GameObject sky = Sky.create(windowDimensions);
//		this.gameObjects().addGameObject(sky, Layer.BACKGROUND);
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 2: Create Terrain and initial ground [0..windowWidth)
//		int seed = new Random().nextInt();
//		Terrain terrain = new Terrain(windowDimensions, seed);
//
//		int initialMinX = 0;
//		int initialMaxX = (int) windowDimensions.x();
//		List<Block> initialBlocks = terrain.createInRange(initialMinX, initialMaxX);
//
//		// DEBUG ‒ print each block’s Y to confirm it’s on‐screen
//		for (Block b : initialBlocks) {
//			System.out.println("DEBUG: Block at Y = " + b.getTopLeftCorner().y());
//			this.gameObjects().addGameObject(b, Layer.STATIC_OBJECTS);
//		}
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 3: Create Flora and initial trees [0..windowWidth)
//		Flora flora = new Flora(terrain::groundHeightAt, seed);
//		List<LayeredObject> initialFlora = flora.createInRange(initialMinX, initialMaxX);
//
//		for (LayeredObject lo : initialFlora) {
//			this.gameObjects().addGameObject(lo.obj(), lo.layer());
//		}
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 4: Spawn Avatar on that ground
//		final int LEFT_MARGIN_X = Block.SIZE * 2;
//		float groundY = terrain.groundHeightAt(LEFT_MARGIN_X);
//		Vector2 avatarPos = new Vector2(LEFT_MARGIN_X, groundY - Avatar.SIZE);
//
//		Avatar avatar = new Avatar(avatarPos, inputListener, imageReader);
//		avatar.setTag("avatar");
//		this.gameObjects().addGameObject(avatar, Layer.DEFAULT);
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 5: Create & set the Camera AFTER avatar is on visible ground
//		Vector2 worldDims = new Vector2(Float.MAX_VALUE, windowDimensions.y());
//		Camera camera = new Camera(
//				avatar,
//				Vector2.ZERO,
//				windowDimensions,
//				worldDims
//		);
//		setCamera(camera);
//
//		// DEBUG ‒ print camera center to confirm it’s on the avatar
//		System.out.println("DEBUG: Camera center = " + camera.getCenter());
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 6: Day/Night cycle + energy bar + cloud
//		float cycleLength = 30f;
//		GameObject nightOverlay = Night.create(windowDimensions, cycleLength);
//		this.gameObjects().addGameObject(nightOverlay, Layer.FOREGROUND);
//
//		GameObject sun = Sun.create(windowDimensions, cycleLength);
//		this.gameObjects().addGameObject(sun, Layer.BACKGROUND);
//
//		GameObject sunHalo = SunHalo.create(sun);
//		this.gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);
//
//		GameObject energyBar = EnergyIndicator.create(avatar);
//		this.gameObjects().addGameObject(energyBar, Layer.BACKGROUND);
//
//		Cloud.create(windowDimensions, this.gameObjects(), avatar);
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 7: Build callbacks for infinite‐mode
//		final float halfWidth = windowDimensions.x() / 2f;
//
//		Supplier<Float> cameraLeftXSupplier = () ->
//				camera.getCenter().x() - halfWidth;
//
//		Supplier<Float> cameraRightXSupplier = () ->
//				camera.getCenter().x() + halfWidth;
//
//		BiConsumer<Integer, Integer> onRangeNeeded = (minX, maxX) -> {
//			// Generate new blocks [minX..maxX)
//			List<Block> moreBlocks = terrain.createInRange(minX, maxX);
//			for (Block b : moreBlocks) {
//				this.gameObjects().addGameObject(b, Layer.STATIC_OBJECTS);
//			}
//			// Generate new trees [minX..maxX)
//			List<LayeredObject> moreFlora = flora.createInRange(minX, maxX);
//			for (LayeredObject lo : moreFlora) {
//				this.gameObjects().addGameObject(lo.obj(), lo.layer());
//			}
//		};
//		// ──────────────────────────────────────────────────────────────────
//
//		// ──────────────────────────────────────────────────────────────────
//		// STEP 8: Finally, attach InfiniteWorldManager with those callbacks + initial range
//		InfiniteWorldManager infiniteManager = new InfiniteWorldManager(
//				cameraLeftXSupplier,
//				cameraRightXSupplier,
//				onRangeNeeded,
//				/*initialMinX=*/ initialMinX,
//				/*initialMaxX=*/ initialMaxX
//		);
//		this.gameObjects().addGameObject(infiniteManager, Layer.BACKGROUND);
//		// ──────────────────────────────────────────────────────────────────
//
//		// Optional debug:
//		// System.out.println("initializeGame() complete");
//	}
//
//	public static void main(String[] args) {
//		new PepseGameManager().run();
//	}
//}
