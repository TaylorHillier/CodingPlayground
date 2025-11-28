package ca.bcit.comp2522.termproject;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * JavaFX-based left-to-right golf game.
 * Orchestrates UI, rendering, course, physics, and scoring.
 *
 * @author Taylor
 * @version 1.0
 */
public final class GolfGameInterface
{
    private static final double CANVAS_WIDTH_PIXELS  = 800.0;
    private static final double CANVAS_HEIGHT_PIXELS = 400.0;

    private static final double BALL_RADIUS_PIXELS = 6.0;

    private static final double CAMERA_CENTER_THRESHOLD_RATIO = 0.4;

    private static final int    NUMBER_OF_TILES_PER_HOLE   = 30;
    private static final double TILE_WIDTH_PIXELS          = 40.0;
    private static final double BASE_GROUND_CENTER_YPixels =
        CANVAS_HEIGHT_PIXELS * 0.75;

    private static final double SAND_DISTANCE_MULTIPLIER    = 0.40; // 60% less far
    private static final double ROUGH_DISTANCE_MULTIPLIER   = 0.70;
    private static final double FAIRWAY_DISTANCE_MULTIPLIER = 1.00;

    private static final double MINIMUM_LAUNCH_ANGLE_DEGREES = 15.0;
    private static final double MAXIMUM_LAUNCH_ANGLE_DEGREES = 70.0;
    private static final double DEFAULT_LAUNCH_ANGLE_DEGREES = 45.0;

    private static final double MINIMUM_POWER_PERCENTAGE = 10.0;
    private static final double MAXIMUM_POWER_PERCENTAGE = 100.0;
    private static final double DEFAULT_POWER_PERCENTAGE = 60.0;

    private static final double AIM_ARROW_BASE_LENGTH_PIXELS  = 80.0;
    private static final double AIM_ARROW_EXTRA_LENGTH_PIXELS = 80.0;
    private static final double AIM_ARROW_HEAD_LENGTH_PIXELS  = 12.0;
    private static final double AIM_ARROW_HEAD_ANGLE_DEGREES  = 160.0;

    private final CountDownLatch gameFinishedLatch;

    private final Random                  randomNumberGenerator;
    private final Map<ClubType, GolfClub> golfClubsByType;

    private Stage           gameStage;
    private Canvas          gameCanvas;
    private GraphicsContext graphicsContext;

    private GolfCourse golfCourse;
    private GolfBall   golfBall;

    private double cameraOffsetXPixels;

    private int     strokesTakenCount;
    private Integer bestScoreFromFile;
    private int     parForHole;

    private AnimationTimer animationTimer;

    // UI controls
    private ComboBox<ClubType> clubSelectionComboBox;
    private Slider             shotPowerSlider;
    private Slider             launchAngleSlider;
    private Label              statusLabel;
    private Label              parAndScoreLabel;

    /**
     * Constructs the GolfGameInterface, initializing clubs, course, ball, and score.
     *
     * @param gameFinishedLatch latch that is counted down when the window closes
     */
    public GolfGameInterface(final CountDownLatch gameFinishedLatch)
    {
        this.gameFinishedLatch = gameFinishedLatch;
        randomNumberGenerator  = new Random();
        golfClubsByType        = new EnumMap<>(ClubType.class);
        cameraOffsetXPixels    = 0.0;
        strokesTakenCount      = 0;
        bestScoreFromFile      = null;
        parForHole             = 0;

        initializeClubs();
        generateNewHole();
        loadBestScoreFromFile();
    }

    /**
     * Opens the golf game in a new JavaFX Stage.
     */
    public void openInNewStage()
    {
        gameStage = new Stage();
        gameStage.setTitle("Simple Golf Game");

        gameCanvas      = new Canvas(CANVAS_WIDTH_PIXELS, CANVAS_HEIGHT_PIXELS);
        graphicsContext = gameCanvas.getGraphicsContext2D();

        final BorderPane rootPane = new BorderPane();
        rootPane.setCenter(gameCanvas);
        rootPane.setBottom(createControlPanel());

        final Scene scene = new Scene(rootPane);
        gameStage.setScene(scene);

        setupAnimationLoop();

        gameStage.setOnCloseRequest(windowEvent ->
                                    {
                                        if (animationTimer != null)
                                        {
                                            animationTimer.stop();
                                        }
                                        gameFinishedLatch.countDown();
                                    });

        gameStage.show();
    }

    // -------------------- Initialization --------------------

    private void initializeClubs()
    {
        golfClubsByType.put(ClubType.DRIVER,
                            new DriverGolfClub("Driver", 260.0));
        golfClubsByType.put(ClubType.WEDGE,
                            new WedgeGolfClub("Wedge", 140.0));
        golfClubsByType.put(ClubType.PUTTER,
                            new PutterGolfClub("Putter", 60.0));
    }

    private void generateNewHole()
    {
        golfCourse = CourseGenerator.generateSingleHole(
            randomNumberGenerator,
            NUMBER_OF_TILES_PER_HOLE,
            TILE_WIDTH_PIXELS,
            BASE_GROUND_CENTER_YPixels
                                                       );

        parForHole = golfCourse.getParStrokes();
        initializeBallAtTee();
    }

    private void initializeBallAtTee()
    {
        final TerrainTile startTerrainTile = golfCourse.getStartTile();

        final double ballStartXPixels =
            startTerrainTile.getStartXPixels() + TILE_WIDTH_PIXELS * 0.25;
        final double ballStartYPixels =
            startTerrainTile.getGroundCenterYPixels() - BALL_RADIUS_PIXELS;

        golfBall = new GolfBall(ballStartXPixels, ballStartYPixels, BALL_RADIUS_PIXELS);

        cameraOffsetXPixels = 0.0;
        strokesTakenCount   = 0;
        updateParAndScoreLabel();
    }

    private HBox createControlPanel()
    {
        final HBox controlPanel = new HBox(10.0);
        controlPanel.setPadding(new Insets(8.0));

        clubSelectionComboBox = new ComboBox<>();
        clubSelectionComboBox.getItems().addAll(ClubType.values());
        clubSelectionComboBox.getSelectionModel().select(ClubType.DRIVER);

        shotPowerSlider = new Slider(
            MINIMUM_POWER_PERCENTAGE,
            MAXIMUM_POWER_PERCENTAGE,
            DEFAULT_POWER_PERCENTAGE
        );
        shotPowerSlider.setShowTickMarks(true);
        shotPowerSlider.setShowTickLabels(true);

        launchAngleSlider = new Slider(
            MINIMUM_LAUNCH_ANGLE_DEGREES,
            MAXIMUM_LAUNCH_ANGLE_DEGREES,
            DEFAULT_LAUNCH_ANGLE_DEGREES
        );
        launchAngleSlider.setShowTickMarks(true);
        launchAngleSlider.setShowTickLabels(true);

        final Button hitBallButton = new Button("Hit");
        hitBallButton.setOnAction(actionEvent -> performShot());

        final Button newHoleButton = new Button("New Hole");
        newHoleButton.setOnAction(actionEvent -> generateNewHole());

        statusLabel      = new Label("Pick a club, angle, power, then hit.");
        parAndScoreLabel = new Label(buildParAndScoreText());

        controlPanel.getChildren().addAll(
            new Label("Club:"), clubSelectionComboBox,
            new Label("Angle:"), launchAngleSlider,
            new Label("Power:"), shotPowerSlider,
            hitBallButton,
            newHoleButton,
            parAndScoreLabel,
            statusLabel
                                         );

        return controlPanel;
    }

    private void setupAnimationLoop()
    {
        animationTimer = new AnimationTimer()
        {
            private long lastUpdateNanoseconds = 0L;

            @Override
            public void handle(final long currentTimeNanoseconds)
            {
                if (lastUpdateNanoseconds == 0L)
                {
                    lastUpdateNanoseconds = currentTimeNanoseconds;
                    return;
                }

                final double deltaTimeSeconds =
                    (currentTimeNanoseconds - lastUpdateNanoseconds) / 1_000_000_000.0;
                lastUpdateNanoseconds = currentTimeNanoseconds;

                updateGameState(deltaTimeSeconds);
                renderGame();
            }
        };

        animationTimer.start();
    }

    // -------------------- Game logic --------------------

    private void performShot()
    {
        if (golfBall.isMoving())
        {
            return;
        }

        final ClubType selectedClubType =
            clubSelectionComboBox.getSelectionModel().getSelectedItem();

        if (selectedClubType == null)
        {
            statusLabel.setText("No club selected.");
            return;
        }

        final GolfClub selectedGolfClub = golfClubsByType.get(selectedClubType);
        final double powerPercentage = shotPowerSlider.getValue();

        final TerrainTile currentTerrainTile =
            golfCourse.getTileAtX(golfBall.getPositionXPixels());

        final double terrainDistanceMultiplier = switch (currentTerrainTile.getTerrainType())
        {
            case SAND -> SAND_DISTANCE_MULTIPLIER;
            case ROUGH -> ROUGH_DISTANCE_MULTIPLIER;
            case WATER -> 0.0;
            default -> FAIRWAY_DISTANCE_MULTIPLIER;
        };

        if (terrainDistanceMultiplier == 0.0)
        {
            initializeBallAtTee();
            statusLabel.setText("Splash! Ball reset to the tee.");
            return;
        }

        final ShotContext shotContext =
            new ShotContext(powerPercentage, terrainDistanceMultiplier);

        final ShotResult shotResult = selectedGolfClub.computeShot(shotContext);

        final double launchAngleDegrees = launchAngleSlider.getValue();
        final double initialSpeedPixelsPerSecond =
            ProjectilePhysics.computeInitialSpeed(
                shotResult.getExpectedHorizontalRangePixels(),
                launchAngleDegrees
                                                 );

        if (initialSpeedPixelsPerSecond <= 0.0)
        {
            statusLabel.setText("Invalid angle for a forward shot.");
            return;
        }

        final double launchAngleRadians = Math.toRadians(launchAngleDegrees);

        final double initialVelocityXPixelsPerSecond =
            initialSpeedPixelsPerSecond * Math.cos(launchAngleRadians);
        final double initialVelocityYPixelsPerSecond =
            -initialSpeedPixelsPerSecond * Math.sin(launchAngleRadians);

        golfBall.launch(initialVelocityXPixelsPerSecond, initialVelocityYPixelsPerSecond);

        strokesTakenCount++;

        statusLabel.setText(
            "Shot with " + selectedGolfClub.getDisplayName()
            + " at " + Math.round(launchAngleDegrees) + "°"
            + " power " + Math.round(powerPercentage) + "%"
                           );
        updateParAndScoreLabel();
    }

    private void updateGameState(final double deltaTimeSeconds)
    {
        if (golfBall.isMoving())
        {
            final TerrainTile currentTerrainTile =
                golfCourse.getTileAtX(golfBall.getPositionXPixels());

            final double groundCenterYPixels =
                currentTerrainTile.getGroundCenterYPixels() - golfBall.getRadiusPixels();

            final boolean stillMoving =
                ProjectilePhysics.updateBallWithTerrain(
                    golfBall,
                    currentTerrainTile,
                    groundCenterYPixels,
                    deltaTimeSeconds
                                                       );

            if (!stillMoving)
            {
                handleBallStop(currentTerrainTile);
            }
        }

        updateCamera();
    }

    private void handleBallStop(final TerrainTile currentTerrainTile)
    {
        final TerrainType terrainType = currentTerrainTile.getTerrainType();

        if (terrainType == TerrainType.WATER)
        {
            initializeBallAtTee();
            statusLabel.setText("Ball rolled into water. Reset to tee.");
        }
        else if (terrainType == TerrainType.HOLE)
        {
            statusLabel.setText("Ball in the hole! Strokes: " + strokesTakenCount);
            updateBestScoreIfNeeded();
        }
        else if (terrainType == TerrainType.SAND)
        {
            statusLabel.setText("Stopped in sand. Next shot is heavily reduced.");
        }
        else
        {
            statusLabel.setText(
                "Ball stopped on " + terrainType.name().toLowerCase() + "."
                               );
        }
    }

    private void updateCamera()
    {
        final double centerThresholdXPixels =
            CANVAS_WIDTH_PIXELS * CAMERA_CENTER_THRESHOLD_RATIO;

        final double relativeBallXPixels =
            golfBall.getPositionXPixels() - cameraOffsetXPixels;

        if (relativeBallXPixels > centerThresholdXPixels)
        {
            cameraOffsetXPixels = golfBall.getPositionXPixels() - centerThresholdXPixels;
        }

        if (cameraOffsetXPixels < 0.0)
        {
            cameraOffsetXPixels = 0.0;
        }
    }

    // -------------------- Rendering --------------------

    private void renderGame()
    {
        graphicsContext.setFill(Color.SKYBLUE);
        graphicsContext.fillRect(0.0, 0.0, CANVAS_WIDTH_PIXELS, CANVAS_HEIGHT_PIXELS);

        final List<TerrainTile> terrainTiles = golfCourse.getTerrainTiles();
        terrainTiles.forEach(this::drawTerrainTile);

        drawBall();

        if (!golfBall.isMoving())
        {
            drawAimArrow();
        }
    }

    private void drawTerrainTile(final TerrainTile terrainTile)
    {
        final double screenStartXPixels =
            terrainTile.getStartXPixels() - cameraOffsetXPixels;
        final double screenEndXPixels =
            terrainTile.getEndXPixels() - cameraOffsetXPixels;
        final double tileWidthOnScreenPixels =
            screenEndXPixels - screenStartXPixels;

        if (screenEndXPixels < 0.0 || screenStartXPixels > CANVAS_WIDTH_PIXELS)
        {
            return;
        }

        switch (terrainTile.getTerrainType())
        {
            case FAIRWAY -> graphicsContext.setFill(Color.GREEN);
            case ROUGH -> graphicsContext.setFill(Color.DARKGREEN);
            case SAND -> graphicsContext.setFill(Color.KHAKI);
            case WATER -> graphicsContext.setFill(Color.DEEPSKYBLUE);
            case HOLE -> graphicsContext.setFill(Color.DARKGREEN);
            default -> graphicsContext.setFill(Color.GRAY);
        }

        final double groundCenterYPixels = terrainTile.getGroundCenterYPixels();

        graphicsContext.fillRect(
            screenStartXPixels,
            groundCenterYPixels,
            tileWidthOnScreenPixels,
            CANVAS_HEIGHT_PIXELS - groundCenterYPixels
                                );

        if (terrainTile.getTerrainType() == TerrainType.HOLE)
        {
            drawFlag(screenStartXPixels, tileWidthOnScreenPixels, groundCenterYPixels);
        }
    }

    private void drawFlag(final double screenStartXPixels,
                          final double tileWidthOnScreenPixels,
                          final double groundCenterYPixels)
    {
        final double flagPoleXPixels = screenStartXPixels + tileWidthOnScreenPixels * 0.5;
        final double flagTopYPixels = groundCenterYPixels - 40.0;

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(flagPoleXPixels, groundCenterYPixels,
                                   flagPoleXPixels, flagTopYPixels);

        graphicsContext.setFill(Color.RED);
        graphicsContext.fillPolygon(
            new double[]{flagPoleXPixels, flagPoleXPixels + 18.0, flagPoleXPixels},
            new double[]{flagTopYPixels, flagTopYPixels + 8.0, flagTopYPixels + 16.0},
            3
                                   );
    }

    private void drawBall()
    {
        final double screenBallXPixels =
            golfBall.getPositionXPixels() - cameraOffsetXPixels;

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillOval(
            screenBallXPixels - BALL_RADIUS_PIXELS,
            golfBall.getPositionYPixels() - BALL_RADIUS_PIXELS,
            BALL_RADIUS_PIXELS * 2.0,
            BALL_RADIUS_PIXELS * 2.0
                                );

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeOval(
            screenBallXPixels - BALL_RADIUS_PIXELS,
            golfBall.getPositionYPixels() - BALL_RADIUS_PIXELS,
            BALL_RADIUS_PIXELS * 2.0,
            BALL_RADIUS_PIXELS * 2.0
                                  );
    }

    private void drawAimArrow()
    {
        final double baseArrowXPixels =
            golfBall.getPositionXPixels() - cameraOffsetXPixels;
        final double baseArrowYPixels =
            golfBall.getPositionYPixels();

        final double launchAngleDegrees = launchAngleSlider.getValue();
        final double launchAngleRadians = Math.toRadians(launchAngleDegrees);

        final double normalizedPower =
            shotPowerSlider.getValue() / MAXIMUM_POWER_PERCENTAGE;

        final double arrowLengthPixels =
            AIM_ARROW_BASE_LENGTH_PIXELS
            + AIM_ARROW_EXTRA_LENGTH_PIXELS * normalizedPower;

        final double arrowEndXPixels =
            baseArrowXPixels + arrowLengthPixels * Math.cos(launchAngleRadians);
        final double arrowEndYPixels =
            baseArrowYPixels - arrowLengthPixels * Math.sin(launchAngleRadians);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(2.0);
        graphicsContext.strokeLine(
            baseArrowXPixels, baseArrowYPixels,
            arrowEndXPixels, arrowEndYPixels
                                  );

        final double arrowHeadAngleOffsetRadians =
            Math.toRadians(AIM_ARROW_HEAD_ANGLE_DEGREES);

        final double leftHeadAngleRadians = launchAngleRadians + arrowHeadAngleOffsetRadians;
        final double rightHeadAngleRadians = launchAngleRadians - arrowHeadAngleOffsetRadians;

        final double leftHeadXPixels =
            arrowEndXPixels + AIM_ARROW_HEAD_LENGTH_PIXELS * Math.cos(leftHeadAngleRadians);
        final double leftHeadYPixels =
            arrowEndYPixels - AIM_ARROW_HEAD_LENGTH_PIXELS * Math.sin(leftHeadAngleRadians);

        final double rightHeadXPixels =
            arrowEndXPixels + AIM_ARROW_HEAD_LENGTH_PIXELS * Math.cos(rightHeadAngleRadians);
        final double rightHeadYPixels =
            arrowEndYPixels - AIM_ARROW_HEAD_LENGTH_PIXELS * Math.sin(rightHeadAngleRadians);

        graphicsContext.strokeLine(arrowEndXPixels, arrowEndYPixels,
                                   leftHeadXPixels, leftHeadYPixels);
        graphicsContext.strokeLine(arrowEndXPixels, arrowEndYPixels,
                                   rightHeadXPixels, rightHeadYPixels);
    }

    // -------------------- Scoring / persistence --------------------

    private String buildParAndScoreText()
    {
        final String bestScoreText =
            (bestScoreFromFile == null)
                ? "No best score yet"
                : "Best: " + bestScoreFromFile;

        final long sandTileCount =
            golfCourse.getTerrainTiles().stream()
                      .filter(tile -> tile.getTerrainType() == TerrainType.SAND)
                      .count();

        final String sandSummary = "Sand tiles: " + sandTileCount;

        return "Par: " + parForHole
               + " | Strokes: " + strokesTakenCount
               + " | " + bestScoreText
               + " | " + sandSummary;
    }

    private void updateParAndScoreLabel()
    {
        if (parAndScoreLabel != null)
        {
            parAndScoreLabel.setText(buildParAndScoreText());
        }
    }

    private void loadBestScoreFromFile()
    {
        bestScoreFromFile = HighScoreStorage.loadBestScore();
    }

    private void updateBestScoreIfNeeded()
    {
        if (bestScoreFromFile == null || strokesTakenCount < bestScoreFromFile)
        {
            bestScoreFromFile = strokesTakenCount;
            HighScoreStorage.saveBestScore(bestScoreFromFile, statusLabel);
        }

        updateParAndScoreLabel();
    }
}
