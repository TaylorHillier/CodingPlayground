package ca.bcit.comp2522.termproject;

import java.util.List;

/**
 * Utility methods for projectile and rolling physics in the golf game.
 *
 * @author Taylor
 * @version 1.0
 */
public final class ProjectilePhysics
{
    private static final double GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED = 420.0;

    private static final double GRAVITY_MULTIPLIER_ASCENT = 0.8;

    private static final double GRAVITY_MULTIPLIER_DESCENT = 1.2;

    private static final double LANDING_BOUNCE_THRESHOLD_PIXELS_PER_SECOND = 80.0;

    private static final double LANDING_BOUNCE_ENERGY_RESTITUTION_FACTOR = 0.35;

    private static final double ROLL_FRICTION_FACTOR_FAIRWAY = 0.96;

    private static final double ROLL_FRICTION_FACTOR_ROUGH = 0.90;

    private static final double ROLL_FRICTION_FACTOR_SAND = 0.80;

    private static final double ROLL_FRICTION_FACTOR_DEFAULT = 0.92;

    private static final double ROLL_STOP_SPEED_THRESHOLD_PIXELS_PER_SECOND = 10.0;

    private static final double PROJECTILE_DOUBLE_ANGLE_MULTIPLIER = 2.0;

    private static final double PROJECTILE_APEX_DIVISOR = 2.0;

    private static final double ZERO_SIN_ANGLE_THRESHOLD = 0.0;

    private static final double MINIMUM_INITIAL_SPEED_PIXELS_PER_SECOND = 0.0;

    private static final double ZERO_VERTICAL_VELOCITY_PIXELS_PER_SECOND = 0.0;

    private static final double AIR_OBSTACLE_HORIZONTAL_RESTITUTION_FACTOR = 0.6;

    private static final double AIR_OBSTACLE_VERTICAL_DAMPING_FACTOR = 0.6;

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

        final double gravityMultiplier;

        final double currentVerticalVelocityPixelsPerSecond;
        currentVerticalVelocityPixelsPerSecond = golfBall.getVelocityYPixelsPerSecond();

        if (currentVerticalVelocityPixelsPerSecond < ZERO_VERTICAL_VELOCITY_PIXELS_PER_SECOND)
        {
            gravityMultiplier = GRAVITY_MULTIPLIER_ASCENT;
        }
        else
        {
            gravityMultiplier = GRAVITY_MULTIPLIER_DESCENT;
        }

        final double effectiveGravityPixelsPerSecondSquared;
        effectiveGravityPixelsPerSecondSquared = GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED * gravityMultiplier;

        golfBall.updateFreeFlight(deltaTimeSeconds, effectiveGravityPixelsPerSecondSquared);

        if (golfBall.getPositionYPixels() >= groundCenterYPixels)
        {
            golfBall.snapToGround(groundCenterYPixels);

            final double absoluteVerticalVelocityPixelsPerSecond;
            absoluteVerticalVelocityPixelsPerSecond = Math.abs(golfBall.getVelocityYPixelsPerSecond());

            if (absoluteVerticalVelocityPixelsPerSecond
                > LANDING_BOUNCE_THRESHOLD_PIXELS_PER_SECOND)
            {
                final double newVerticalVelocityPixelsPerSecond;
                newVerticalVelocityPixelsPerSecond = -golfBall.getVelocityYPixelsPerSecond()
                                                     * LANDING_BOUNCE_ENERGY_RESTITUTION_FACTOR;

                golfBall.setVelocityYPixelsPerSecond(newVerticalVelocityPixelsPerSecond);
            }
            else
            {
                golfBall.setVelocityYPixelsPerSecond(ZERO_VERTICAL_VELOCITY_PIXELS_PER_SECOND);

                final double rollFriction;
                final double newHorizontalVelocityPixelsPerSecond;
                final double absoluteHorizontalVelocityPixelsPerSecond;

                rollFriction =
                    switch (terrainTile.getTerrainType())
                    {
                        case SAND -> ROLL_FRICTION_FACTOR_SAND;
                        case ROUGH -> ROLL_FRICTION_FACTOR_ROUGH;
                        case FAIRWAY, HOLE -> ROLL_FRICTION_FACTOR_FAIRWAY;
                        default -> ROLL_FRICTION_FACTOR_DEFAULT;
                    };

                newHorizontalVelocityPixelsPerSecond = golfBall.getVelocityXPixelsPerSecond() * rollFriction;

                golfBall.setVelocityXPixelsPerSecond(newHorizontalVelocityPixelsPerSecond);

                absoluteHorizontalVelocityPixelsPerSecond = Math.abs(golfBall.getVelocityXPixelsPerSecond());

                if (absoluteHorizontalVelocityPixelsPerSecond < ROLL_STOP_SPEED_THRESHOLD_PIXELS_PER_SECOND)
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
        final double launchAngleRadians;
        final double sinDoubleAngle;
        final double initialSpeedSquaredPixelsPerSecondSquared;

        launchAngleRadians = Math.toRadians(launchAngleDegrees);

        sinDoubleAngle = Math.sin(PROJECTILE_DOUBLE_ANGLE_MULTIPLIER * launchAngleRadians);

        if (sinDoubleAngle <= ZERO_SIN_ANGLE_THRESHOLD)
        {
            return MINIMUM_INITIAL_SPEED_PIXELS_PER_SECOND;
        }

        initialSpeedSquaredPixelsPerSecondSquared = targetRangePixels
                                                    * GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED
                                                    / sinDoubleAngle;

        return Math.sqrt(initialSpeedSquaredPixelsPerSecondSquared);
    }

    /**
     * Computes the maximum vertical height offset for terrain generation so that
     * a full-power wedge at the maximum launch angle can still clear the
     * highest cliffs. Uses the club's max range and gravity.
     *
     * @param wedgeGolfClub                wedge club to use
     * @param maximumPowerPercentage       max power used for range
     * @param fairwayDistanceMultiplier    terrain multiplier for fairway
     * @param maximumLaunchAngleDegrees    maximum launch angle in degrees
     * @param courseHeightSafetyFactor     safety factor to shrink the theoretical max
     * @param minimumMaxHeightOffsetPixels minimum height if physics fails
     * @return safe maximum height offset in pixels
     */
    public static double computeMaximumHeightOffsetForCourse(
        final GolfClub wedgeGolfClub,
        final double maximumPowerPercentage,
        final double fairwayDistanceMultiplier,
        final double maximumLaunchAngleDegrees,
        final double courseHeightSafetyFactor,
        final double minimumMaxHeightOffsetPixels)
    {
        final ShotContext fullPowerShotContext;
        final ShotResult fullPowerWedgeShotResult;

        fullPowerShotContext     = new ShotContext(maximumPowerPercentage, fairwayDistanceMultiplier);
        fullPowerWedgeShotResult = wedgeGolfClub.computeShot(fullPowerShotContext);

        final double maximumWedgeRangePixels;
        final double gravityPixelsPerSecondSquared;
        final double launchAngleRadians;
        final double sinDoubleAngle;
        final double initialSpeedSquaredPixelsPerSecondSquared;

        maximumWedgeRangePixels = fullPowerWedgeShotResult.getExpectedHorizontalRangePixels();

        gravityPixelsPerSecondSquared = getGravityAccelerationPixelsPerSecondSquared();

        launchAngleRadians = Math.toRadians(maximumLaunchAngleDegrees);

        sinDoubleAngle = Math.sin(PROJECTILE_DOUBLE_ANGLE_MULTIPLIER * launchAngleRadians);

        if (sinDoubleAngle <= ZERO_SIN_ANGLE_THRESHOLD)
        {
            return minimumMaxHeightOffsetPixels;
        }

        // range = v^2 * sin(2θ) / g  -> v^2 = range * g / sin(2θ)
        initialSpeedSquaredPixelsPerSecondSquared = maximumWedgeRangePixels
                                                    * gravityPixelsPerSecondSquared
                                                    / sinDoubleAngle;

        final double sinAngle;
        sinAngle = Math.sin(launchAngleRadians);

        // H = v^2 * sin^2(θ) / (2g)
        final double maximumApexHeightPixels;
        maximumApexHeightPixels = (initialSpeedSquaredPixelsPerSecondSquared
                                   * sinAngle * sinAngle)
                                  / (PROJECTILE_APEX_DIVISOR * gravityPixelsPerSecondSquared);

        final double safeMaximumHeightOffsetPixels;
        safeMaximumHeightOffsetPixels = maximumApexHeightPixels * courseHeightSafetyFactor;

        return Math.max(minimumMaxHeightOffsetPixels, safeMaximumHeightOffsetPixels);
    }

    /**
     * Handles collisions between the golf ball and a collection of air obstacles.
     * Applies a simple bounce effect by reflecting and damping the ball's velocity.
     *
     * @param golfBall     the golf ball whose motion is being simulated
     * @param airObstacles list of air obstacles to test for collisions
     * @return {@code true} if a collision occurred; {@code false} otherwise
     */
    public static boolean handleAirObstacleCollisions(final GolfBall golfBall,
                                                      final List<AirObstacle> airObstacles)
    {
        if (airObstacles == null || airObstacles.isEmpty())
        {
            return false;
        }

        final double ballCenterXPixels;
        final double ballCenterYPixels;
        final double ballRadiusPixels;

        ballCenterXPixels = golfBall.getPositionXPixels();

        ballCenterYPixels = golfBall.getPositionYPixels();

        ballRadiusPixels = golfBall.getRadiusPixels();

        for (final AirObstacle airObstacle : airObstacles)
        {
            final double closestXPixels;
            final double closestYPixels;
            final double deltaXPixels;
            final double deltaYPixels;
            final double distanceSquaredPixels;

            closestXPixels = clamp(ballCenterXPixels,
                                   airObstacle.getLeftXPixels(),
                                   airObstacle.getRightXPixels());

            closestYPixels = clamp(ballCenterYPixels,
                                   airObstacle.getTopYPixels(),
                                   airObstacle.getBottomYPixels());

            deltaXPixels = ballCenterXPixels - closestXPixels;

            deltaYPixels = ballCenterYPixels - closestYPixels;

            distanceSquaredPixels = deltaXPixels * deltaXPixels + deltaYPixels * deltaYPixels;

            if (distanceSquaredPixels <= ballRadiusPixels * ballRadiusPixels)
            {
                final double currentVelocityXPixelsPerSecond;
                final double currentVelocityYPixelsPerSecond;

                currentVelocityXPixelsPerSecond = golfBall.getVelocityXPixelsPerSecond();
                currentVelocityYPixelsPerSecond = golfBall.getVelocityYPixelsPerSecond();

                // Decide which side we hit: top/bottom vs left/right.
                final boolean hitTop;
                final boolean hitBottom;

                hitTop =
                    (ballCenterYPixels <= airObstacle.getTopYPixels())
                    && (closestYPixels == airObstacle.getTopYPixels());

                hitBottom =
                    (ballCenterYPixels >= airObstacle.getBottomYPixels())
                    && (closestYPixels == airObstacle.getBottomYPixels());

                if (hitTop || hitBottom)
                {
                    // Vertical collision (top or bottom of the block)
                    // → flip vertical velocity, lightly damp horizontal.
                    final double newVerticalVelocityPixelsPerSecond;
                    final double newHorizontalVelocityPixelsPerSecond;

                    newVerticalVelocityPixelsPerSecond =
                        -currentVelocityYPixelsPerSecond * AIR_OBSTACLE_VERTICAL_DAMPING_FACTOR;

                    newHorizontalVelocityPixelsPerSecond =
                        currentVelocityXPixelsPerSecond * AIR_OBSTACLE_HORIZONTAL_RESTITUTION_FACTOR;

                    golfBall.setVelocityYPixelsPerSecond(newVerticalVelocityPixelsPerSecond);
                    golfBall.setVelocityXPixelsPerSecond(newHorizontalVelocityPixelsPerSecond);
                }
                else
                {
                    // Side collision (left or right)
                    // → flip horizontal velocity, lightly damp vertical.
                    final double newHorizontalVelocityXPixelsPerSecond;
                    final double newVerticalVelocityYPixelsPerSecond;

                    newHorizontalVelocityXPixelsPerSecond =
                        -currentVelocityXPixelsPerSecond * AIR_OBSTACLE_HORIZONTAL_RESTITUTION_FACTOR;

                    newVerticalVelocityYPixelsPerSecond =
                        currentVelocityYPixelsPerSecond * AIR_OBSTACLE_VERTICAL_DAMPING_FACTOR;

                    golfBall.setVelocityXPixelsPerSecond(newHorizontalVelocityXPixelsPerSecond);
                    golfBall.setVelocityYPixelsPerSecond(newVerticalVelocityYPixelsPerSecond);
                }

                return true;
            }

        }

        return false;
    }

    /**
     * Clamps a value between a minimum and maximum.
     *
     * @param value   the value to clamp
     * @param minimum the minimum allowed value
     * @param maximum the maximum allowed value
     * @return the clamped value between {@code minimum} and {@code maximum}
     */
    private static double clamp(final double value,
                                final double minimum,
                                final double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /**
     * Gets the gravity acceleration used in the projectile calculations.
     *
     * @return gravity acceleration in pixels per second squared
     */
    public static double getGravityAccelerationPixelsPerSecondSquared()
    {
        return GRAVITY_ACCELERATION_PIXELS_PER_SECOND_SQUARED;
    }
}
