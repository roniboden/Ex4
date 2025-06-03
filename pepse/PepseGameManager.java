package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;

import pepse.world.Block;
import pepse.world.Terrain;
import pepse.world.Sky;
import pepse.world.Avatar;
import pepse.world.EnergyIndicator;

import pepse.world.pepse.world.daynight.Night;
import pepse.world.pepse.world.daynight.Sun;
import pepse.world.pepse.world.daynight.SunHalo;
import pepse.world.pepse.world.daynight.Cloud;

import java.util.List;
import java.util.Random;
import danogl.collisions.Layer;
import pepse.world.pepse.world.trees.Tree;

public class PepseGameManager extends GameManager {

	@Override
	public void initializeGame(
			ImageReader imageReader,
			SoundReader soundReader,
			UserInputListener inputListener,
			WindowController windowController) {

		super.initializeGame(imageReader, soundReader, inputListener, windowController);

		Vector2 windowDimensions = windowController.getWindowDimensions();

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
		Random rand = new Random(seed);
		float blockSize = Block.SIZE;
		int startX = 0;
		int endX = (int) windowDimensions.x();

		for (float x = startX; x < endX; x += blockSize) {
			if (rand.nextDouble() < 0.1) {  // 10% chance for a tree at each column
				Tree.plantTree(gameObjects(), x, terrain, rand);
			}
		}



	}
		public static void main(String[] args) {
		new PepseGameManager().run();
	}
}
