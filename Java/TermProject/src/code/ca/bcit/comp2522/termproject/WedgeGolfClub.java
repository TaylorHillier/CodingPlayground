package ca.bcit.comp2522.termproject;

/**
 * Wedge club: medium distance, slightly better from bad lies.
 */
public final class WedgeGolfClub extends AbstractGolfClub
{
    public WedgeGolfClub(final String displayName,
                         final double baseDistancePixels)
    {
        super(displayName, baseDistancePixels);
    }

    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        return rawDistancePixels * 1.1;
    }
}

