package pepse.world;

import danogl.collisions.Layer;

/**
 * Marks a {@link danogl.GameObject GameObject} as having a preferred default
 * layer in the scene graph.
 *
 * <p>Classes that implement this interface can be queried by utility code
 * (e.g.&nbsp;{@code Flora}) to decide in which {@link Layer} the object should
 * be inserted, without hard-coding layer assignments in multiple places.</p>
 * @author Noa
 */
public interface LayerProvider {

	/**
	 * @return the layer that best suits this object
	 */
	int defaultLayer();
}
