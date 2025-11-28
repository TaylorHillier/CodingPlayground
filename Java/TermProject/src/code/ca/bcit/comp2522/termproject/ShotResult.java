package ca.bcit.comp2522.termproject;

/**
 * Represents the expected outcome of a golf shot in flat terrain.
 *
 * @author Taylor
 * @version 1.0
 */
public final class ShotResult
{
    private final double expectedHorizontalRangePixels;

    /**
     * Constructs a ShotResult.
     *
     * @param expectedHorizontalRangePixels the predicted horizontal range on flat ground in pixels
     */
    public ShotResult(final double expectedHorizontalRangePixels)
    {
        this.expectedHorizontalRangePixels = expectedHorizontalRangePixels;
    }

    public double getExpectedHorizontalRangePixels()
    {
        return expectedHorizontalRangePixels;
    }
}
