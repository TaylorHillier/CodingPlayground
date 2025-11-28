package ca.bcit.comp2522.termproject;

/**
 * Represents the state of the golf ball in world-space coordinates (pixels).
 * Stores position, velocity, and motion flags.
 *
 * @author Taylor
 * @version 1.0
 */
public final class GolfBall
{
    private final double radiusPixels;

    private double positionXPixels;
    private double positionYPixels;

    private double velocityXPixelsPerSecond;
    private double velocityYPixelsPerSecond;

    private boolean moving;

    /**
     * Constructs a GolfBall with an initial position and radius.
     *
     * @param initialPositionXPixels the starting x-coordinate in pixels
     * @param initialPositionYPixels the starting y-coordinate in pixels
     * @param radiusPixels           the radius of the ball in pixels
     */
    public GolfBall(final double initialPositionXPixels,
                    final double initialPositionYPixels,
                    final double radiusPixels)
    {
        this.radiusPixels = radiusPixels;

        positionXPixels = initialPositionXPixels;
        positionYPixels = initialPositionYPixels;

        velocityXPixelsPerSecond = 0.0;
        velocityYPixelsPerSecond = 0.0;

        moving = false;
    }

    /**
     * Resets the ball to a new tee position and stops all motion.
     *
     * @param newPositionXPixels the x-coordinate of the tee in pixels
     * @param newPositionYPixels the y-coordinate of the tee in pixels
     */
    public void resetToTee(final double newPositionXPixels,
                           final double newPositionYPixels)
    {
        positionXPixels = newPositionXPixels;
        positionYPixels = newPositionYPixels;

        velocityXPixelsPerSecond = 0.0;
        velocityYPixelsPerSecond = 0.0;

        moving = false;
    }

    /**
     * Sets the ball in motion with a new velocity.
     *
     * @param newVelocityXPixelsPerSecond velocity in the x direction, in pixels per second
     * @param newVelocityYPixelsPerSecond velocity in the y direction, in pixels per second
     */
    public void launch(final double newVelocityXPixelsPerSecond,
                       final double newVelocityYPixelsPerSecond)
    {
        velocityXPixelsPerSecond = newVelocityXPixelsPerSecond;
        velocityYPixelsPerSecond = newVelocityYPixelsPerSecond;
        moving                   = true;
    }

    /**
     * Integrates the ball's motion under the given vertical acceleration.
     * Does not know anything about terrain or collision; it only moves
     * position and velocity.
     *
     * @param deltaTimeSeconds                           elapsed time in seconds
     * @param verticalAccelerationPixelsPerSecondSquared vertical acceleration in pixels per second squared
     */
    public void updateFreeFlight(final double deltaTimeSeconds,
                                 final double verticalAccelerationPixelsPerSecondSquared)
    {
        if (!moving)
        {
            return;
        }

        velocityYPixelsPerSecond += verticalAccelerationPixelsPerSecondSquared * deltaTimeSeconds;

        positionXPixels += velocityXPixelsPerSecond * deltaTimeSeconds;
        positionYPixels += velocityYPixelsPerSecond * deltaTimeSeconds;
    }

    /**
     * Snaps the ball onto the ground y-position and optionally stops vertical motion.
     *
     * @param groundCenterYPixels the y-coordinate of the ground at the ball's x in pixels
     */
    public void snapToGround(final double groundCenterYPixels)
    {
        positionYPixels = groundCenterYPixels;
    }

    public double getPositionXPixels()
    {
        return positionXPixels;
    }

    public double getPositionYPixels()
    {
        return positionYPixels;
    }

    public double getVelocityXPixelsPerSecond()
    {
        return velocityXPixelsPerSecond;
    }

    public void setVelocityXPixelsPerSecond(final double newVelocityXPixelsPerSecond)
    {
        velocityXPixelsPerSecond = newVelocityXPixelsPerSecond;
    }

    public double getVelocityYPixelsPerSecond()
    {
        return velocityYPixelsPerSecond;
    }

    public void setVelocityYPixelsPerSecond(final double newVelocityYPixelsPerSecond)
    {
        velocityYPixelsPerSecond = newVelocityYPixelsPerSecond;
    }

    public double getRadiusPixels()
    {
        return radiusPixels;
    }

    public boolean isMoving()
    {
        return moving;
    }

    public void stop()
    {
        moving                   = false;
        velocityXPixelsPerSecond = 0.0;
        velocityYPixelsPerSecond = 0.0;
    }
}
