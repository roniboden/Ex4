package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.pepse.world.daynight.Night;
import pepse.world.pepse.world.daynight.Sun;

import java.util.List;
import java.util.Random;

public class PepseGameManager extends GameManager {


	@Override
	public void initializeGame(ImageReader imageReader, SoundReader soundReader,
							   UserInputListener inputListener,
							   WindowController windowController) {
		super.initializeGame(imageReader, soundReader, inputListener, windowController);
		Vector2 windowDimensions = windowController.getWindowDimensions();
		//creates the Sky
		GameObject sky=  Sky.create(windowDimensions);
		this.gameObjects().addGameObject(sky, Layer.BACKGROUND);
		//creates th ground
		int seed = new Random().nextInt();
		Terrain terrain= new Terrain(windowDimensions,seed);
		List<Block> groundBlocks = terrain.createInRange(0, (int) windowDimensions.x());
		for (Block block : groundBlocks) {
			this.gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
		}

		Vector2 windowSize = windowController.getWindowDimensions();
		float cycleLength = 30f;
		//Creates Night
		GameObject nightOverlay = Night.create(windowSize, cycleLength);
		this.gameObjects().addGameObject(nightOverlay, Layer.FOREGROUND);
		//Creates Sun
		GameObject sun = Sun.create(windowSize, cycleLength);
		this.gameObjects().addGameObject(sun, Layer.BACKGROUND);

	}

    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}



//TEST FOR NIGHT
//Vector2 windowSize = windowController.getWindowDimensions();
//
// Add a blue background (just to have something visible)
//RectangleRenderable blueBg = new RectangleRenderable(Color.BLUE);
//GameObject background = new GameObject(
//		Vector2.ZERO,
//		windowSize,
//		blueBg
//);
//background.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
//this.gameObjects().addGameObject(background, Layer.BACKGROUND);
//
// Now add the Night overlay on top
//GameObject night = Night.create(windowSize, 10f); // 10-second cycle
//this.gameObjects().addGameObject(night, Layer.FOREGROUND);
