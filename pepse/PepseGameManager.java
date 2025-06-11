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
 * @author Noa & Roni
 */
public class PepseGameManager extends GameManager {
	/**
	 * The life cycle duration of the day/night cycle in seconds.
	 * This value determines how long it takes for the game to transition from day to night and back.
	 */
	private static final float LIFE_CYCLE = 30f ;
	private static final String AVATAR_TAG = "avatar";
	/**
	 * The Flora object that manages the trees and other flora in the game.
	 */
	private Flora flora;
	/**
	 * The Terrain object that represents the ground blocks in the game.
	 */
	private Terrain terrain;
	private Avatar avatar;

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
		// The seed is made once per run and determines the objects location
		int seed = new Random().nextInt();
		//int seed = 1000;
		this.terrain = new Terrain(windowDimensions, seed);
		List<Block> groundBlocks = terrain.createInRange(0, (int) windowDimensions.x());
		for (Block block : groundBlocks) {
			this.gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
		}

		// 3. Create Night overlay (30-second cycle)
		float cycleLength = LIFE_CYCLE;
		GameObject nightOverlay = Night.create(windowDimensions, cycleLength);
		this.gameObjects().addGameObject(nightOverlay, Layer.FOREGROUND);

		// 4. Create Sun and SunHalo
		GameObject sun = Sun.create(windowDimensions, cycleLength);
		this.gameObjects().addGameObject(sun, Layer.BACKGROUND);

		GameObject sunHalo = SunHalo.create(sun);
		this.gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);
		
		addAvatar(inputListener,imageReader);

		// 6. Create EnergyIndicator
		GameObject energyBar = EnergyIndicator.create(avatar);
		this.gameObjects().addGameObject(energyBar, Layer.BACKGROUND);

		// 7. Create Cloud (which will rain when avatar jumps underneath)
		Cloud.create(windowDimensions, this.gameObjects(), avatar);

		// 8. Plant trees randomly across the terrain
		addTrees(windowDimensions,  seed);
		/* (1) build camera exactly as § 9.1 */
		Camera camera = new Camera(
				avatar, Vector2.ZERO,
				windowController.getWindowDimensions(),
				windowController.getWindowDimensions()
		);
		setCamera(camera);

		/* (2) helper lambdas for InfiniteWorldManager */
		addIninityWorld(windowController, camera,
				initialMinX, initialMaxX
		);

	}
	/**
	 * Adds trees to the game world by randomly planting them across the terrain.
	 *
	 * @param windowDimensions The dimensions of the game window,
	 *                           used to determine the range for planting trees.
	 * @param seed             A random seed used to ensure consistent tree placement across game runs.
	 */
	private void addTrees(Vector2 windowDimensions, int seed) {
        //Plant trees randomly across the terrain
		int startX = 0;
		int endX   = (int) windowDimensions.x();

		this.flora = new Flora(terrain::groundHeightAt, seed);

		/* ---- declare with the new type ---- */
		List<Flora.LayeredObject> floraObjects = flora.createInRange(startX, endX);

		/* ---- add each object to its preferred layer ---- */
		for (Flora.LayeredObject lo : floraObjects) {
			gameObjects().addGameObject(lo.obj(), lo.layer());

		}

	}
/**
	 * Adds an InfiniteWorldManager to the game,
 * which manages the dynamic loading and unloading of terrain blocks
	 * and flora as the camera moves.
	 *
	 * @param windowController The WindowController used to get the window dimensions for camera calcs.
	 * @param camera           The Camera that tracks the avatar's position.
	 * @param initialMinX     The initial minimum X coordinate of the terrain range already built.
	 * @param initialMaxX     The initial maximum X coordinate of the terrain range already built.
	 */
	private void addIninityWorld(WindowController windowController,
	                            Camera camera,
	                             int initialMinX, int initialMaxX
	                            ) {
		float halfW= windowController.getWindowDimensions().x() / 2f;
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
	private void addAvatar(UserInputListener inputListener,
	                       ImageReader imageReader)  {
		final int LEFT_MARGIN_X = Block.SIZE * 2;
		float groundY = terrain.groundHeightAt(LEFT_MARGIN_X);
		float avatarHeight = Avatar.SIZE;
		Vector2 avatarPos = new Vector2(LEFT_MARGIN_X, groundY - avatarHeight);
		 avatar = new Avatar(avatarPos, inputListener, imageReader);
		avatar.setTag(AVATAR_TAG);
		this.gameObjects().addGameObject(avatar, Layer.DEFAULT);

		// 6. Create EnergyIndicator
		GameObject energyBar = EnergyIndicator.create(avatar);
		this.gameObjects().addGameObject(energyBar, Layer.BACKGROUND);
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
