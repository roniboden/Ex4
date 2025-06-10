package pepse.world;

/**
 * Supplies the y-coordinate of the terrain surface for a given world-space
 * {@code x}-coordinate.
 *
 * <p>This functional interface decouples code that needs to know “where the
 * ground is” (e.g.&nbsp;tree and avatar placement) from the concrete
 * implementation that generates the terrain.
 * Implementations are expected to be
 * <em>pure</em>: calling {@link #groundHeightAt(float)} with the same
 * {@code x} must always return the same value for a given world seed.</p>
 * @author Noa
 */
public interface GroundHeightProvider {

	/**
	 * Returns the y-coordinate (world space) of the terrain surface directly
	 * below the supplied {@code x}-coordinate.
	 *
	 * @param x world-space x-coordinate
	 * @return  y-coordinate of the topmost ground block at {@code x}
	 */
	float groundHeightAt(float x);
}
