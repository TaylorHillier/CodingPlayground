package ca.bcit.comp2522.termproject;

/**
 * Represents a wedge golf club, providing medium-distance shots and
 * slightly enhanced performance on non-ideal terrain.
 *
 * <p>This class extends {@link AbstractGolfClub} and applies a consistent
 * distance bonus to all computed raw distances.</p>
 * <p>
 * author Taylor
 * version 1.0
 */
public final class WedgeGolfClub extends AbstractGolfClub
{
    /**
     * Base reference yardage for a wedge, expressed in pixels.
     */
    public static final double WEDGE_YARDAGE = 250.0;

    private static final double WEDGE_DISTANCE_MULTIPLIER = 1.10;

    /**
     * Constructs a {@code WedgeGolfClub} using the given display name and base distance.
     *
     * @param displayNameParameter        name shown for this club
     * @param baseDistancePixelsParameter base distance for a wedge shot, in pixels
     */
    public WedgeGolfClub(final String displayNameParameter,
                         final double baseDistancePixelsParameter)
    {
        super(displayNameParameter, baseDistancePixelsParameter);
    }

    /**
     * Adjusts the computed raw shot distance by applying the wedge's distance multiplier.
     *
     * @param rawDistancePixels the original computed distance in pixels
     * @param shotContext       context describing power and terrain multipliers (unused)
     * @return adjusted distance in pixels after applying the wedge multiplier
     */
    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        final double adjustedDistancePixels;
        adjustedDistancePixels = rawDistancePixels * WEDGE_DISTANCE_MULTIPLIER;

        return adjustedDistancePixels;
    }
}
