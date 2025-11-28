package ca.bcit.comp2522.termproject;

/**
 * Represents the state of the golf ball in world-space coordinates (pixels),
 * including position, velocity, and movement state.
 * <p>
 * The ball is mutable during gameplay and can be launched, updated, or reset
 * to safe positions. All coordinates and velocities are stored in pixels.
 *
 * @author Taylor
 * @version 1.0
 */
public final class GolfBall
{
    private static final double ZERO_VELOCITY_PIXELS_PER_SECOND = 0.0;

    private final double radiusPixels;

    private double positionXPixels;
    private double positionYPixels;

    private double velocityXPixelsPerSecond;
    private double velocityYPixelsPerSecond;

    private double safePositionXPixels;
    private double safePositionYPixels;

    private boolean moving;

    /**
     * Constructs a GolfBall with an initial position and radius.
     *
     * @param initialPositionXPixels initial x-position in pixels
     * @param initialPositionYPixels initial y-position in pixels
     * @param radiusPixels           radius of the ball in pixels
     */
    public GolfBall(final double initialPositionXPixels,
                    final double initialPositionYPixels,
                    final double radiusPixels)
    {
        this.radiusPixels = radiusPixels;

        positionXPixels = initialPositionXPixels;
        positionYPixels = initialPositionYPixels;

        velocityXPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
        velocityYPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;

        moving = false;

        safePositionXPixels = initialPositionXPixels;
        safePositionYPixels = initialPositionYPixels;
    }

    /**
     * Marks the current position of the ball as the safe fallback location.
     */
    public void markSafePosition()
    {
        safePositionXPixels = positionXPixels;
        safePositionYPixels = positionYPixels;
    }

    /**
     * Resets the ball to its most recently marked safe position and stops movement.
     */
    public void resetToSafePosition()
    {
        positionXPixels = safePositionXPixels;
        positionYPixels = safePositionYPixels;

        velocityXPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
        velocityYPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
    }

    /**
     * Resets the ball to a tee position and stops all motion.
     *
     * @param teePositionXPixels tee x-position in pixels
     * @param teePositionYPixels tee y-position in pixels
     */
    public void resetToTee(final double teePositionXPixels,
                           final double teePositionYPixels)
    {
        positionXPixels = teePositionXPixels;
        positionYPixels = teePositionYPixels;

        velocityXPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
        velocityYPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;

        moving = false;
    }

    /**
     * Launches the ball with the specified velocity components.
     *
     * @param newVelocityXPixelsPerSecond horizontal velocity in pixels per second
     * @param newVelocityYPixelsPerSecond vertical velocity in pixels per second
     */
    public void launch(final double newVelocityXPixelsPerSecond,
                       final double newVelocityYPixelsPerSecond)
    {
        velocityXPixelsPerSecond = newVelocityXPixelsPerSecond;
        velocityYPixelsPerSecond = newVelocityYPixelsPerSecond;

        moving = true;
    }

    /**
     * Updates the ball's free-flight motion under a vertical acceleration.
     *
     * @param deltaTimeSeconds                           elapsed time in seconds
     * @param verticalAccelerationPixelsPerSecondSquared acceleration in pixels per second squared
     */
    public void updateFreeFlight(final double deltaTimeSeconds,
                                 final double verticalAccelerationPixelsPerSecondSquared)
    {
        if (!moving)
        {
            return;
        }

        velocityYPixelsPerSecond +=
            verticalAccelerationPixelsPerSecondSquared * deltaTimeSeconds;

        positionXPixels += velocityXPixelsPerSecond * deltaTimeSeconds;
        positionYPixels += velocityYPixelsPerSecond * deltaTimeSeconds;
    }

    /**
     * Snaps the ball onto the ground y-position.
     *
     * @param groundCenterYPixels ground y-position in pixels
     */
    public void snapToGround(final double groundCenterYPixels)
    {
        positionYPixels = groundCenterYPixels;
    }

    /**
     * Returns the current x-position of the ball in pixels.
     *
     * @return x-position in pixels
     */
    public double getPositionXPixels()
    {
        return positionXPixels;
    }

    /**
     * Returns the current y-position of the ball in pixels.
     *
     * @return y-position in pixels
     */
    public double getPositionYPixels()
    {
        return positionYPixels;
    }

    /**
     * Returns the horizontal velocity in pixels per second.
     *
     * @return horizontal velocity in px/s
     */
    public double getVelocityXPixelsPerSecond()
    {
        return velocityXPixelsPerSecond;
    }

    /**
     * Sets the horizontal velocity of the ball.
     *
     * @param newVelocityXPixelsPerSecond new horizontal velocity in px/s
     */
    public void setVelocityXPixelsPerSecond(final double newVelocityXPixelsPerSecond)
    {
        velocityXPixelsPerSecond = newVelocityXPixelsPerSecond;
    }

    /**
     * Returns the vertical velocity in pixels per second.
     *
     * @return vertical velocity in px/s
     */
    public double getVelocityYPixelsPerSecond()
    {
        return velocityYPixelsPerSecond;
    }

    /**
     * Sets the vertical velocity of the ball.
     *
     * @param newVelocityYPixelsPerSecond new vertical velocity in px/s
     */
    public void setVelocityYPixelsPerSecond(final double newVelocityYPixelsPerSecond)
    {
        velocityYPixelsPerSecond = newVelocityYPixelsPerSecond;
    }

    /**
     * Returns the radius of the ball in pixels.
     *
     * @return radius in pixels
     */
    public double getRadiusPixels()
    {
        return radiusPixels;
    }

    /**
     * Returns whether the ball is currently moving.
     *
     * @return true if moving, false otherwise
     */
    public boolean isMoving()
    {
        return moving;
    }

    /**
     * Stops all ball movement and zeroes out velocities.
     */
    public void stop()
    {
        moving = false;

        velocityXPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
        velocityYPixelsPerSecond = ZERO_VELOCITY_PIXELS_PER_SECOND;
    }
}
