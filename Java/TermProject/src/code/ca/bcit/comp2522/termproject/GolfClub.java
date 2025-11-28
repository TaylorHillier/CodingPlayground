package ca.bcit.comp2522.termproject;

/**
 * Strategy interface for golf clubs. Each club computes a shot
 * differently based on the ShotContext.
 *
 * @author Taylor
 * @version 1.0
 */
public interface GolfClub
{
    /**
     * Returns the display name of this club, such as "Driver".
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Computes the expected flat-ground shot outcome for this club.
     *
     * @param shotContext the context describing power and terrain multipliers
     * @return a ShotResult describing the expected horizontal range
     */
    ShotResult computeShot(final ShotContext shotContext);
}
