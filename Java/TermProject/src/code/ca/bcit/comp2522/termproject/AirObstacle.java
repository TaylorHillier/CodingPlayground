package ca.bcit.comp2522.termproject;

/**
 * Represents a simple axis-aligned rectangular obstacle suspended in the air.
 * A {@code GolfBall} may collide with this obstacle during flight.
 * <p>
 * Each value is stored in pixels and describes the world-space bounds
 * of the obstacle. The obstacle is immutable once created.
 * <p>
 *
 * @author Taylor
 * @version 1.0
 */
public final class AirObstacle
{
    private final double leftXPixels;

    private final double rightXPixels;

    private final double topYPixels;

    private final double bottomYPixels;

    /**
     * Constructs an AirObstacle using world-space pixel coordinates.
     *
     * @param leftXPixels   left boundary of the rectangle in pixels
     * @param rightXPixels  right boundary of the rectangle in pixels
     * @param topYPixels    top boundary of the rectangle in pixels
     * @param bottomYPixels bottom boundary of the rectangle in pixels
     */
    public AirObstacle(final double leftXPixels,
                       final double rightXPixels,
                       final double topYPixels,
                       final double bottomYPixels)
    {
        this.leftXPixels   = leftXPixels;
        this.rightXPixels  = rightXPixels;
        this.topYPixels    = topYPixels;
        this.bottomYPixels = bottomYPixels;
    }

    /**
     * Returns the left x-coordinate of the obstacle in pixels.
     *
     * @return left boundary in pixels
     */
    public double getLeftXPixels()
    {
        return leftXPixels;
    }

    /**
     * Returns the right x-coordinate of the obstacle in pixels.
     *
     * @return right boundary in pixels
     */
    public double getRightXPixels()
    {
        return rightXPixels;
    }

    /**
     * Returns the top y-coordinate of the obstacle in pixels.
     *
     * @return top boundary in pixels
     */
    public double getTopYPixels()
    {
        return topYPixels;
    }

    /**
     * Returns the bottom y-coordinate of the obstacle in pixels.
     *
     * @return bottom boundary in pixels
     */
    public double getBottomYPixels()
    {
        return bottomYPixels;
    }
}
