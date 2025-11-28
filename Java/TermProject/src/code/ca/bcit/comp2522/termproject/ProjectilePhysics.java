package ca.bcit.comp2522.termproject;

/**
 * Utility methods for projectile and rolling physics in the golf game.
 *
 * @author Taylor
 * @version 1.0
 */
public final class ProjectilePhysics
{
    private static final double GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED = 420.0;
    private static final double GRAVITY_MULTIPLIER_ASCENT                      = 0.8;
    private static final double GRAVITY_MULTIPLIER_DESCENT                     = 1.2;

    private static final double LANDING_BOUNCE_THRESHOLD_PIXELS_PER_SECOND = 80.0;

    private static final double ROLL_FRICTION_FACTOR_FAIRWAY = 0.96;
    private static final double ROLL_FRICTION_FACTOR_ROUGH   = 0.90;
    private static final double ROLL_FRICTION_FACTOR_SAND    = 0.80;
    private static final double ROLL_FRICTION_FACTOR_DEFAULT = 0.92;

    private ProjectilePhysics()
    {
        // Utility class; prevent instantiation.
    }

    /**
     * Updates the ball's motion including gravity, simple bounce, and rolling friction.
     * This method assumes the caller will provide the terrain tile and ground height.
     *
     * @param golfBall            the ball to update
     * @param terrainTile         the tile under the ball
     * @param groundCenterYPixels the y-position of the ground in pixels
     * @param deltaTimeSeconds    elapsed time in seconds
     * @return true if the ball is still moving after this update; false if it stopped
     */
    public static boolean updateBallWithTerrain(final GolfBall golfBall,
                                                final TerrainTile terrainTile,
                                                final double groundCenterYPixels,
                                                final double deltaTimeSeconds)
    {
        if (!golfBall.isMoving())
        {
            return false;
        }

        final double gravityMultiplier =
            (golfBall.getVelocityYPixelsPerSecond() < 0.0)
                ? GRAVITY_MULTIPLIER_ASCENT
                : GRAVITY_MULTIPLIER_DESCENT;

        final double effectiveGravityPixelsPerSecondSquared =
            GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED * gravityMultiplier;

        golfBall.updateFreeFlight(deltaTimeSeconds, effectiveGravityPixelsPerSecondSquared);

        if (golfBall.getPositionYPixels() >= groundCenterYPixels)
        {
            golfBall.snapToGround(groundCenterYPixels);

            if (Math.abs(golfBall.getVelocityYPixelsPerSecond())
                > LANDING_BOUNCE_THRESHOLD_PIXELS_PER_SECOND)
            {
                golfBall.setVelocityYPixelsPerSecond(
                    -golfBall.getVelocityYPixelsPerSecond() * 0.35);
            }
            else
            {
                golfBall.setVelocityYPixelsPerSecond(0.0);

                final double rollFriction =
                    switch (terrainTile.getTerrainType())
                    {
                        case SAND -> ROLL_FRICTION_FACTOR_SAND;
                        case ROUGH -> ROLL_FRICTION_FACTOR_ROUGH;
                        case FAIRWAY, HOLE -> ROLL_FRICTION_FACTOR_FAIRWAY;
                        default -> ROLL_FRICTION_FACTOR_DEFAULT;
                    };

                golfBall.setVelocityXPixelsPerSecond(
                    golfBall.getVelocityXPixelsPerSecond() * rollFriction);

                if (Math.abs(golfBall.getVelocityXPixelsPerSecond()) < 10.0)
                {
                    golfBall.stop();
                }
            }
        }

        return golfBall.isMoving();
    }

    /**
     * Computes initial projectile speed for a target horizontal range with a given angle.
     *
     * @param targetRangePixels  desired horizontal range in pixels
     * @param launchAngleDegrees launch angle in degrees
     * @return initial speed in pixels per second
     */
    public static double computeInitialSpeed(final double targetRangePixels,
                                             final double launchAngleDegrees)
    {
        final double launchAngleRadians = Math.toRadians(launchAngleDegrees);
        final double sinDoubleAngle = Math.sin(2.0 * launchAngleRadians);

        if (sinDoubleAngle <= 0.0)
        {
            return 0.0;
        }

        return Math.sqrt(
            targetRangePixels
            * GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED
            / sinDoubleAngle
                        );
    }
}
