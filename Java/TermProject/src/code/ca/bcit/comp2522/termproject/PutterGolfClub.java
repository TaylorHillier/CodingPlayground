package ca.bcit.comp2522.termproject;

/**
 * Represents a putter golf club, which provides short and consistent shot distances.
 * The putter caps the maximum distance to keep shots controlled.
 *
 * <p>This class extends {@link AbstractGolfClub} and applies a fixed upper bound
 * to the computed shot distance.</p>
 *
 * @author Taylor
 * @version 1.0
 */
public final class PutterGolfClub extends AbstractGolfClub
{
    /**
     * Base distance in pixels for a putter shot before applying power scaling.
     */
    public static final double PUTTER_BASE_DISTANCE_PIXELS = 150.0;

    private static final double MAXIMUM_PUTTER_DISTANCE_PIXELS = 250.0;

    /**
     * Constructs a {@code PutterGolfClub} with the given display name and base distance.
     *
     * @param displayNameParameter        name shown for this club
     * @param baseDistancePixelsParameter base distance in pixels
     */
    public PutterGolfClub(final String displayNameParameter,
                          final double baseDistancePixelsParameter)
    {
        super(displayNameParameter, baseDistancePixelsParameter);
    }

    /**
     * Ensures the putter's shot distance never exceeds its maximum allowed distance.
     *
     * @param rawDistancePixels the unadjusted computed distance in pixels
     * @param shotContext       context describing power and terrain multipliers
     * @return the capped shot distance in pixels
     */
    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        final double cappedDistancePixels;
        cappedDistancePixels = Math.min(rawDistancePixels, MAXIMUM_PUTTER_DISTANCE_PIXELS);

        return cappedDistancePixels;
    }
}
