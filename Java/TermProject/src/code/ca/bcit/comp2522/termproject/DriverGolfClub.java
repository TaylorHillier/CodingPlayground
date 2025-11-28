package ca.bcit.comp2522.termproject;

/**
 * Driver club: long distance, best from the fairway.
 */
public final class DriverGolfClub extends AbstractGolfClub
{
    public DriverGolfClub(final String displayName,
                          final double baseDistancePixels)
    {
        super(displayName, baseDistancePixels);
    }

    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        return rawDistancePixels;
    }
}

