package ca.bcit.comp2522.termproject;

/**
 * Putter club: short and consistent. Distance is capped.
 */
public final class PutterGolfClub extends AbstractGolfClub
{
    private static final double MAX_PUTTER_DISTANCE_PIXELS = 80.0;

    public PutterGolfClub(final String displayName,
                          final double baseDistancePixels)
    {
        super(displayName, baseDistancePixels);
    }

    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        return Math.min(rawDistancePixels, MAX_PUTTER_DISTANCE_PIXELS);
    }
}
