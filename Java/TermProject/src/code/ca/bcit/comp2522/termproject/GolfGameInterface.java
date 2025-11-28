package ca.bcit.comp2522.termproject;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
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

    private static final double BASE_GROUND_CENTER_Y_RATIO = 0.75;

    private static final double CAMERA_CENTER_THRESHOLD_RATIO = 0.4;

    private static final int    NUMBER_OF_TILES_PER_HOLE = 30;
    private static final double TILE_WIDTH_PIXELS        = 40.0;

    private static final double SAND_DISTANCE_MULTIPLIER    = 0.40;
    private static final double ROUGH_DISTANCE_MULTIPLIER   = 0.70;
    private static final double FAIRWAY_DISTANCE_MULTIPLIER = 1.00;

    private static final double MINIMUM_LAUNCH_ANGLE_DEGREES = 5.0;
    private static final double MAXIMUM_LAUNCH_ANGLE_DEGREES = 175.0;
    private static final double DEFAULT_LAUNCH_ANGLE_DEGREES = 45.0;

    private static final double MAXIMUM_POWER_PERCENTAGE = 100.0;

    private static final double AIM_ARROW_BASE_LENGTH_PIXELS  = 80.0;
    private static final double AIM_ARROW_EXTRA_LENGTH_PIXELS = 80.0;
    private static final double AIM_ARROW_HEAD_LENGTH_PIXELS  = 12.0;
    private static final double AIM_ARROW_HEAD_ANGLE_DEGREES  = 160.0;

    private static final double COURSE_HEIGHT_SAFETY_FACTOR      = 0.70;
    private static final double MINIMUM_MAX_HEIGHT_OFFSET_PIXELS = 40.0;

    private static final double POWER_CHARGE_RATE_PERCENT_PER_SECOND = 70.0;

    private static final int NUMBER_OF_HOLES_PER_ROUND = 18;

    private static final double CONTROL_PANEL_SPACING_PIXELS                    = 10.0;
    private static final double CONTROL_PANEL_PADDING_PIXELS                    = 8.0;
    private static final double ALTER_BY_ONE                                    = 1.0;
    private static final double MINIMUM_AIM_DELTA_X_PIXELS                      = 0.001;
    private static final double MINIMUM_INITIAL_SPEED_EPSILON_PIXELS_PER_SECOND = 0.01;

    private static final double BALL_RADIUS_MULTIPLIER                = 2.0;
    private static final int    NEXT_HOLE_INDEX                       = 1;
    private static final double TEE_BALL_OFFSET_RATIO_FROM_TILE_START = 0.25;

    private static final double FLAG_HEIGHT_PIXELS                  = 40.0;
    private static final double FLAG_TRIANGLE_OFFSET_X_PIXELS       = 18.0;
    private static final double FLAG_TRIANGLE_OFFSET_Y_SMALL_PIXELS = 8.0;
    private static final double FLAG_TRIANGLE_OFFSET_Y_LARGE_PIXELS = 16.0;

    private static final double ARROW_LINE_WIDTH_PIXELS = 2.0;
    private static final double FLAG_POLE_X_MULTIPLIER  = 0.5;

    private static final int POLYGON_FILL = 3;

    private final CountDownLatch gameFinishedLatch;

    private final Random                  randomNumberGenerator;
    private final Map<ClubType, GolfClub> golfClubsByType;

    private Stage           gameStage;
    private Canvas          gameCanvas;
    private GraphicsContext graphicsContext;

    private final List<GolfCourse> golfCourses;
    private final List<Integer>    parPerHole;
    private final List<Integer>    strokesPerHole;
    private       int              currentHoleIndex;

    private Integer bestRoundRelativeToPar;

    private GolfCourse golfCourse;
    private GolfBall   golfBall;

    private double cameraOffsetXPixels;

    private int strokesTakenCount;
    private int parForHole;

    private AnimationTimer animationTimer;

    private ComboBox<ClubType> clubSelectionComboBox;
    private double             currentAimAngleDegrees;
    private double             currentPowerPercentage;
    private boolean            chargingPower;

    private Label statusLabel;
    private Label parAndScoreLabel;

    private static final double INIT_TO_ZERO_DOUBLE = 0.0;
    private static final int    INIT_TO_ZERO_INT    = 0;

    {
        randomNumberGenerator = new Random();
        golfClubsByType       = new EnumMap<>(ClubType.class);

        cameraOffsetXPixels = INIT_TO_ZERO_DOUBLE;
        strokesTakenCount   = INIT_TO_ZERO_INT;
        parForHole          = INIT_TO_ZERO_INT;

        currentAimAngleDegrees = DEFAULT_LAUNCH_ANGLE_DEGREES;
        currentPowerPercentage = INIT_TO_ZERO_DOUBLE;
        chargingPower          = false;

        golfCourses      = new ArrayList<>();
        parPerHole       = new ArrayList<>();
        strokesPerHole   = new ArrayList<>();
        currentHoleIndex = INIT_TO_ZERO_INT;
    }

    /**
     * Constructs the GolfGameInterface, initializing clubs, course, ball, and score.
     *
     * @param gameFinishedLatch latch that is counted down when the window closes
     */
    public GolfGameInterface(final CountDownLatch gameFinishedLatch)
    {
        this.gameFinishedLatch = gameFinishedLatch;
        bestRoundRelativeToPar = HighScoreStorage.loadBestRoundRelativeToPar();
    }

    /**
     * Opens the golf game in a new JavaFX Stage.
     */
    public void openInNewStage()
    {
        final BorderPane rootPane;
        final Scene scene;

        gameStage = new Stage();
        gameStage.setTitle("Simple Golf Game");

        gameCanvas = new Canvas(CANVAS_WIDTH_PIXELS, CANVAS_HEIGHT_PIXELS);
        gameCanvas.getStyleClass().add("game-canvas");

        graphicsContext = gameCanvas.getGraphicsContext2D();

        rootPane = new BorderPane();
        rootPane.getStyleClass().add("game-root");
        rootPane.setCenter(gameCanvas);
        rootPane.setBottom(createControlPanel());

        scene = new Scene(rootPane);

        final var cssResource = GolfGameInterface.class.getResource("/styles.css");
        if (cssResource != null)
        {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }
        else
        {
            System.err.println("WARNING: styles.css not found.");
        }

        gameStage.setScene(scene);

        gameCanvas.setFocusTraversable(true);
        gameCanvas.requestFocus();

        gameCanvas.setOnMouseMoved(this::handleMouseMoved);

        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        generateNewRound();
        updateParAndScoreLabel();

        setupAnimationLoop();

        gameStage.setOnCloseRequest(_ ->
                                    {
                                        if (animationTimer != null)
                                        {
                                            animationTimer.stop();
                                        }
                                        gameFinishedLatch.countDown();
                                    });

        gameStage.show();
    }

    private void initializeClubs()
    {
        golfClubsByType.put(ClubType.DRIVER,
                            new DriverGolfClub("Driver", DriverGolfClub.DRIVER_YARDAGE));
        golfClubsByType.put(ClubType.WEDGE,
                            new WedgeGolfClub("Wedge", WedgeGolfClub.WEDGE_YARDAGE));
        golfClubsByType.put(ClubType.PUTTER,
                            new PutterGolfClub("Putter", PutterGolfClub.PUTTER_BASE_DISTANCE_PIXELS));
    }

    private void generateNewRound()
    {
        final double maximumHeightOffsetPixels;

        golfCourses.clear();
        parPerHole.clear();
        strokesPerHole.clear();
        currentHoleIndex = INIT_TO_ZERO_INT;

        maximumHeightOffsetPixels = ProjectilePhysics.computeMaximumHeightOffsetForCourse(
            golfClubsByType.get(ClubType.WEDGE),
            MAXIMUM_POWER_PERCENTAGE,
            FAIRWAY_DISTANCE_MULTIPLIER,
            MAXIMUM_LAUNCH_ANGLE_DEGREES,
            COURSE_HEIGHT_SAFETY_FACTOR,
            MINIMUM_MAX_HEIGHT_OFFSET_PIXELS);

        for (int holeIndex = 0; holeIndex < NUMBER_OF_HOLES_PER_ROUND; holeIndex++)
        {
            final GolfCourse generatedHole;
            final int parForThisHole;

            generatedHole = CourseGenerator.generateSingleHole(
                randomNumberGenerator,
                NUMBER_OF_TILES_PER_HOLE,
                TILE_WIDTH_PIXELS,
                CANVAS_HEIGHT_PIXELS * BASE_GROUND_CENTER_Y_RATIO,
                maximumHeightOffsetPixels);

            parForThisHole = generatedHole.computePar(
                golfClubsByType,
                MAXIMUM_POWER_PERCENTAGE,
                FAIRWAY_DISTANCE_MULTIPLIER);

            golfCourses.add(generatedHole);
            parPerHole.add(parForThisHole);
            strokesPerHole.add(INIT_TO_ZERO_INT);
        }

        startHole(INIT_TO_ZERO_INT);
    }

    private void startHole(final int holeIndex)
    {
        currentHoleIndex = holeIndex;
        golfCourse       = golfCourses.get(holeIndex);
        parForHole       = parPerHole.get(holeIndex);

        initializeBallAtTee();
        statusLabel.setText(
            "Hole " + (holeIndex + NEXT_HOLE_INDEX) + " of " + NUMBER_OF_HOLES_PER_ROUND
            + ". Move mouse to aim, hold SPACE to charge, release to hit."
                           );
    }

    private void handleMouseMoved(final MouseEvent mouseEvent)
    {
        final double mouseScreenXPixels;
        final double mouseScreenYPixels;

        final double mouseWorldXPixels;
        final double mouseWorldYPixels;

        final double ballWorldXPixels;
        final double ballWorldYPixels;

        double deltaXPixels;
        double deltaYPixels;

        double aimAngleDegrees;

        gameCanvas.requestFocus();

        if (golfBall.isMoving())
        {
            return;
        }

        mouseScreenXPixels = mouseEvent.getX();
        mouseScreenYPixels = mouseEvent.getY();

        mouseWorldXPixels = mouseScreenXPixels + cameraOffsetXPixels;
        mouseWorldYPixels = mouseScreenYPixels;

        ballWorldXPixels = golfBall.getPositionXPixels();
        ballWorldYPixels = golfBall.getPositionYPixels();

        deltaXPixels = mouseWorldXPixels - ballWorldXPixels;
        deltaYPixels = ballWorldYPixels - mouseWorldYPixels;

        final boolean isDeltaXTooSmall =
            Math.abs(deltaXPixels) < MINIMUM_AIM_DELTA_X_PIXELS;

        if (isDeltaXTooSmall)
        {
            final double directionSign;

            if (deltaXPixels >= INIT_TO_ZERO_DOUBLE)
            {
                directionSign = ALTER_BY_ONE;
            }
            else
            {
                directionSign = -ALTER_BY_ONE;
            }

            deltaXPixels = directionSign * MINIMUM_AIM_DELTA_X_PIXELS;
        }

        aimAngleDegrees = Math.toDegrees(Math.atan2(deltaYPixels, deltaXPixels));

        if (aimAngleDegrees < MINIMUM_LAUNCH_ANGLE_DEGREES)
        {
            aimAngleDegrees = MINIMUM_LAUNCH_ANGLE_DEGREES;
        }
        else if (aimAngleDegrees > MAXIMUM_LAUNCH_ANGLE_DEGREES)
        {
            aimAngleDegrees = MAXIMUM_LAUNCH_ANGLE_DEGREES;
        }

        final ClubType selectedClub = clubSelectionComboBox.getSelectionModel().getSelectedItem();
        currentAimAngleDegrees = aimAngleDegrees;
    }

    private void handleKeyPressed(final KeyEvent keyEvent)
    {
        if (keyEvent.getCode() == KeyCode.SPACE)
        {
            if (!chargingPower && !golfBall.isMoving())
            {
                chargingPower          = true;
                currentPowerPercentage = INIT_TO_ZERO_DOUBLE;
                statusLabel.setText("Charging shot power... release SPACE to hit.");
            }
        }
    }

    private void handleKeyReleased(final KeyEvent keyEvent)
    {
        if (keyEvent.getCode() == KeyCode.SPACE)
        {
            if (chargingPower && !golfBall.isMoving())
            {
                chargingPower = false;
                performShot();
            }
        }
    }

    private void initializeBallAtTee()
    {
        final List<TerrainTile> terrainTiles;
        TerrainTile teeTerrainTile;

        final double ballStartXPixels;
        final double ballStartYPixels;

        terrainTiles = golfCourse.getTerrainTiles();

        teeTerrainTile = TerrainTileUtils.findFirstTileOfType(
            terrainTiles,
            TerrainType.FAIRWAY
                                                             );

        if (teeTerrainTile == null)
        {
            teeTerrainTile = golfCourse.getStartTile();
        }

        ballStartXPixels =
            teeTerrainTile.getStartXPixels()
            + TILE_WIDTH_PIXELS * TEE_BALL_OFFSET_RATIO_FROM_TILE_START;

        ballStartYPixels =
            teeTerrainTile.getGroundCenterYPixels() - BALL_RADIUS_PIXELS;

        if (golfBall == null)
        {
            golfBall = new GolfBall(ballStartXPixels, ballStartYPixels, BALL_RADIUS_PIXELS);
        }
        else
        {
            golfBall.resetToTee(ballStartXPixels, ballStartYPixels);
        }

        golfBall.markSafePosition();

        cameraOffsetXPixels    = INIT_TO_ZERO_DOUBLE;
        strokesTakenCount      = INIT_TO_ZERO_INT;
        currentPowerPercentage = INIT_TO_ZERO_DOUBLE;
        chargingPower          = false;

        updateParAndScoreLabel();
    }

    private HBox createControlPanel()
    {
        final HBox controlPanel;
        final Button newRoundButton;
        final Label clubLabel;

        controlPanel = new HBox(CONTROL_PANEL_SPACING_PIXELS);
        controlPanel.setPadding(new Insets(CONTROL_PANEL_PADDING_PIXELS));
        controlPanel.getStyleClass().add("game-control-bar");

        clubSelectionComboBox = new ComboBox<>();
        clubSelectionComboBox.getItems().addAll(ClubType.values());
        clubSelectionComboBox.getSelectionModel().select(ClubType.DRIVER);
        clubSelectionComboBox.getStyleClass().add("club-selector");

        clubSelectionComboBox.setOnAction(_ -> gameCanvas.requestFocus());

        newRoundButton = new Button("New Round");
        newRoundButton.setOnAction(_ ->
                                   {
                                       generateNewRound();
                                       gameCanvas.requestFocus();
                                   });
        newRoundButton.getStyleClass().add("primary-button");

        statusLabel = new Label(
            "Move mouse to aim, hold SPACE to charge, release to hit."
        );
        statusLabel.getStyleClass().add("status-label");

        parAndScoreLabel = new Label("");
        parAndScoreLabel.getStyleClass().add("score-label");

        clubLabel = new Label("Club:");
        clubLabel.getStyleClass().add("control-label");

        controlPanel.getChildren().addAll(
            clubLabel,
            clubSelectionComboBox,
            newRoundButton,
            parAndScoreLabel,
            statusLabel
                                         );

        return controlPanel;
    }

    private void setupAnimationLoop()
    {
        animationTimer = new AnimationTimer()
        {
            private long lastUpdateNanoseconds = INIT_TO_ZERO_INT;

            @Override
            public void handle(final long currentTimeNanoseconds)
            {
                final double deltaTimeSeconds;

                if (lastUpdateNanoseconds == INIT_TO_ZERO_INT)
                {
                    lastUpdateNanoseconds = currentTimeNanoseconds;
                    return;
                }

                deltaTimeSeconds =
                    (currentTimeNanoseconds - lastUpdateNanoseconds) / 1_000_000_000.0;

                lastUpdateNanoseconds = currentTimeNanoseconds;

                updateGameState(deltaTimeSeconds);
                renderGame();
            }
        };

        animationTimer.start();
    }

    private void performShot()
    {
        final ClubType selectedClubType;
        final GolfClub selectedGolfClub;

        final TerrainTile terrainTileUnderBall;
        final double terrainDistanceMultiplier;

        final ShotContext shotContext;
        final ShotResult shotResult;

        double launchAngleDegrees;
        final double initialSpeedPixelsPerSecond;

        double initialVelocityXPixelsPerSecond;
        double initialVelocityYPixelsPerSecond;

        if (golfBall.isMoving())
        {
            return;
        }

        if (currentPowerPercentage <= INIT_TO_ZERO_DOUBLE)
        {
            statusLabel.setText("No power charged. Hold SPACE before releasing.");
            return;
        }

        selectedClubType = clubSelectionComboBox.getSelectionModel().getSelectedItem();

        if (selectedClubType == null)
        {
            statusLabel.setText("No club selected.");
            return;
        }

        selectedGolfClub = golfClubsByType.get(selectedClubType);

        terrainTileUnderBall = golfCourse.getTileAtX(golfBall.getPositionXPixels());

        terrainDistanceMultiplier = switch (terrainTileUnderBall.getTerrainType())
        {
            case SAND -> SAND_DISTANCE_MULTIPLIER;
            case ROUGH -> ROUGH_DISTANCE_MULTIPLIER;
            case WATER -> INIT_TO_ZERO_DOUBLE;
            default -> FAIRWAY_DISTANCE_MULTIPLIER;
        };

        if (terrainDistanceMultiplier == INIT_TO_ZERO_DOUBLE)
        {
            golfBall.resetToSafePosition();
            statusLabel.setText("Splash! Ball reset to the tee.");
            recenterCameraIfBallOffscreen();
            return;
        }

        shotContext = new ShotContext(currentPowerPercentage, terrainDistanceMultiplier);

        shotResult = selectedGolfClub.computeShot(shotContext);

        launchAngleDegrees = currentAimAngleDegrees;

        if (selectedClubType == ClubType.PUTTER)
        {
            initialSpeedPixelsPerSecond = shotResult.getExpectedHorizontalRangePixels();

            if (initialSpeedPixelsPerSecond <= MINIMUM_INITIAL_SPEED_EPSILON_PIXELS_PER_SECOND)
            {
                statusLabel.setText("Putter shot power too low.");
                return;
            }
        }
        else
        {
            // Use a symmetric acute angle for speed calculation so backwards shots still work.
            final double rightAngleDegrees;
            rightAngleDegrees = 90.0;

            double effectiveLaunchAngleDegrees;
            effectiveLaunchAngleDegrees = launchAngleDegrees;

            if (effectiveLaunchAngleDegrees > rightAngleDegrees)
            {
                // Map 100° → 80°, 150° → 30°, etc.
                effectiveLaunchAngleDegrees = (2.0 * rightAngleDegrees) - effectiveLaunchAngleDegrees;
            }

            final double computedInitialSpeedPixelsPerSecond;
            computedInitialSpeedPixelsPerSecond = ProjectilePhysics.computeInitialSpeed(
                shotResult.getExpectedHorizontalRangePixels(),
                effectiveLaunchAngleDegrees);

            initialSpeedPixelsPerSecond = computedInitialSpeedPixelsPerSecond;

            if (initialSpeedPixelsPerSecond <= MINIMUM_INITIAL_SPEED_EPSILON_PIXELS_PER_SECOND)
            {
                statusLabel.setText("Invalid aim. Adjust your aim angle.");
                return;
            }
        }

        final double launchAngleRadians;
        launchAngleRadians = Math.toRadians(launchAngleDegrees);

        initialVelocityXPixelsPerSecond = initialSpeedPixelsPerSecond * Math.cos(launchAngleRadians);

        initialVelocityYPixelsPerSecond = -initialSpeedPixelsPerSecond * Math.sin(launchAngleRadians);

        if (selectedClubType == ClubType.PUTTER)
        {
            initialVelocityYPixelsPerSecond = INIT_TO_ZERO_DOUBLE;
        }

        golfBall.launch(initialVelocityXPixelsPerSecond, initialVelocityYPixelsPerSecond);

        strokesTakenCount++;
        currentPowerPercentage = INIT_TO_ZERO_DOUBLE;

        statusLabel.setText(
            "Shot with " + selectedGolfClub.getDisplayName()
            + " at " + Math.round(launchAngleDegrees) + "°"
            + " power " + Math.round(shotContext.getPowerPercentage()) + "%"
                           );
        updateParAndScoreLabel();
    }


    private void finishRoundAndUpdateHighScore()
    {
        int totalPar;
        int totalStrokes;
        int relativeToPar;

        totalPar     = INIT_TO_ZERO_INT;
        totalStrokes = INIT_TO_ZERO_INT;

        for (int holeIndex = 0; holeIndex < NUMBER_OF_HOLES_PER_ROUND; holeIndex++)
        {
            totalPar += parPerHole.get(holeIndex);
            totalStrokes += strokesPerHole.get(holeIndex);
        }

        relativeToPar = totalStrokes - totalPar;

        if (bestRoundRelativeToPar == null || relativeToPar < bestRoundRelativeToPar)
        {
            bestRoundRelativeToPar = relativeToPar;
            HighScoreStorage.saveBestRoundRelativeToPar(bestRoundRelativeToPar, statusLabel);
        }

        statusLabel.setText(
            "Round complete! Total strokes: " + totalStrokes
            + " vs par " + totalPar
            + " (" + formatRelativeToPar(relativeToPar) + ")."
                           );
        updateParAndScoreLabel();
    }

    private String formatRelativeToPar(final int relativeToPar)
    {
        if (relativeToPar == INIT_TO_ZERO_INT)
        {
            return "E";
        }

        if (relativeToPar > INIT_TO_ZERO_INT)
        {
            return "+" + relativeToPar;
        }

        return Integer.toString(relativeToPar);
    }

    private void updateGameState(final double deltaTimeSeconds)
    {
        if (chargingPower && !golfBall.isMoving())
        {
            currentPowerPercentage += POWER_CHARGE_RATE_PERCENT_PER_SECOND * deltaTimeSeconds;

            if (currentPowerPercentage > MAXIMUM_POWER_PERCENTAGE)
            {
                currentPowerPercentage = MAXIMUM_POWER_PERCENTAGE;
            }

            statusLabel.setText("Charging... power " + Math.round(currentPowerPercentage) + "%");
        }

        if (golfBall.isMoving())
        {
            updateMovingBall(deltaTimeSeconds);
        }

        updateCamera();
    }

    private void updateMovingBall(final double deltaTimeSeconds)
    {
        final TerrainTile terrainTileBeforeUpdate;
        final double groundCenterYPixels;

        final boolean stillMoving;

        terrainTileBeforeUpdate = golfCourse.getTileAtX(golfBall.getPositionXPixels());

        groundCenterYPixels = terrainTileBeforeUpdate.getGroundCenterYPixels() - golfBall.getRadiusPixels();

        stillMoving = ProjectilePhysics.updateBallWithTerrain(
            golfBall,
            terrainTileBeforeUpdate,
            groundCenterYPixels,
            deltaTimeSeconds
                                                             );

        if (ProjectilePhysics.handleAirObstacleCollisions(golfBall, golfCourse.getAirObstacles()))
        {
            statusLabel.setText("Ball hit an air obstacle!");
        }

        final TerrainTile terrainTileAfterUpdate = golfCourse.getTileAtX(golfBall.getPositionXPixels());

        if (handleWaterCollisionIfNeeded(terrainTileAfterUpdate))
        {
            return;
        }

        if (handleOutOfBoundsIfNeeded())
        {
            return;
        }

        if (!stillMoving)
        {
            handleBallStop(terrainTileAfterUpdate);
        }
    }

    private boolean handleWaterCollisionIfNeeded(final TerrainTile terrainTileAfterUpdate)
    {
        if (terrainTileAfterUpdate.getTerrainType() != TerrainType.WATER)
        {
            return false;
        }

        final double waterSurfaceYPixels;
        final double ballBottomYPixels;

        waterSurfaceYPixels = terrainTileAfterUpdate.getGroundCenterYPixels();

        ballBottomYPixels = golfBall.getPositionYPixels() + golfBall.getRadiusPixels();

        if (ballBottomYPixels < waterSurfaceYPixels)
        {
            return false;
        }

        golfBall.stop();
        golfBall.resetToSafePosition();
        statusLabel.setText("Splash! Ball reset to last safe position.");
        recenterCameraIfBallOffscreen();

        return true;
    }

    private boolean handleOutOfBoundsIfNeeded()
    {
        final TerrainTile lastTerrainTile;
        final TerrainTile firstTerrainTile;

        final double lastTileEndXPixels;
        final double firstTileXPixels;

        lastTerrainTile    = golfCourse.getLastTile();
        firstTerrainTile   = golfCourse.getStartTile();
        lastTileEndXPixels = lastTerrainTile.getEndXPixels();
        firstTileXPixels   = firstTerrainTile.getStartXPixels();

        if (golfBall.getPositionXPixels() <= lastTileEndXPixels && golfBall.getPositionXPixels() >= firstTileXPixels)
        {
            return false;
        }

        golfBall.stop();
        golfBall.resetToSafePosition();
        statusLabel.setText("Ball went out of bounds past the hole. Reset to last safe position.");
        recenterCameraIfBallOffscreen();

        return true;
    }

    private void handleBallStop(final TerrainTile currentTerrainTile)
    {
        final TerrainType terrainType;
        final TerrainTile lastTerrainTile;

        final boolean ballPastEnd;

        terrainType = currentTerrainTile.getTerrainType();

        lastTerrainTile = golfCourse.getLastTile();
        ballPastEnd     = golfBall.getPositionXPixels() > lastTerrainTile.getEndXPixels();

        if (!ballPastEnd && terrainType != TerrainType.WATER)
        {
            golfBall.markSafePosition();
        }

        if (ballPastEnd)
        {
            golfBall.resetToSafePosition();
            statusLabel.setText("Ball went out of bounds past the hole. Reset to last safe position.");
        }
        else if (terrainType == TerrainType.WATER)
        {
            golfBall.resetToSafePosition();
            statusLabel.setText("Ball rolled into water. Reset to last safe position.");
        }
        else if (terrainType == TerrainType.HOLE || terrainType == TerrainType.GREEN)
        {
            strokesPerHole.set(currentHoleIndex, strokesTakenCount);

            statusLabel.setText(
                "Hole " + (currentHoleIndex + NEXT_HOLE_INDEX)
                + " complete in " + strokesTakenCount
                + " strokes (par " + parForHole + ")."
                               );

            if (currentHoleIndex + NEXT_HOLE_INDEX < NUMBER_OF_HOLES_PER_ROUND)
            {
                startHole(currentHoleIndex + NEXT_HOLE_INDEX);
            }
            else
            {
                finishRoundAndUpdateHighScore();
            }
        }
        else if (terrainType == TerrainType.SAND)
        {
            statusLabel.setText("Stopped in sand. Next shot is heavily reduced.");
        }
        else
        {
            statusLabel.setText("Ball stopped on " + terrainType.name().toLowerCase() + ".");
        }
    }

    private void updateCamera()
    {
        final double centerThresholdXPixels;
        final double relativeBallXPixels;

        centerThresholdXPixels = CANVAS_WIDTH_PIXELS * CAMERA_CENTER_THRESHOLD_RATIO;

        relativeBallXPixels = golfBall.getPositionXPixels() - cameraOffsetXPixels;

        if (relativeBallXPixels > centerThresholdXPixels)
        {
            cameraOffsetXPixels = golfBall.getPositionXPixels() - centerThresholdXPixels;
        }

        if (cameraOffsetXPixels < INIT_TO_ZERO_DOUBLE)
        {
            cameraOffsetXPixels = INIT_TO_ZERO_DOUBLE;
        }
    }

    private int computeTotalParUpToCurrentHole()
    {
        int totalPar;

        totalPar = INIT_TO_ZERO_INT;

        for (int holeIndex = 0; holeIndex <= currentHoleIndex && holeIndex < parPerHole.size(); holeIndex++)
        {
            totalPar += parPerHole.get(holeIndex);
        }

        return totalPar;
    }

    private int computeTotalStrokesUpToCurrentHole()
    {
        int totalStrokes;

        totalStrokes = INIT_TO_ZERO_INT;

        for (int holeIndex = 0; holeIndex < currentHoleIndex; holeIndex++)
        {
            totalStrokes += strokesPerHole.get(holeIndex);
        }

        totalStrokes += strokesTakenCount;

        return totalStrokes;
    }

    private void recenterCameraIfBallOffscreen()
    {
        final double ballScreenXPixels;
        final boolean offLeft;
        final boolean offRight;

        ballScreenXPixels = golfBall.getPositionXPixels() - cameraOffsetXPixels;

        offLeft  = ballScreenXPixels < INIT_TO_ZERO_DOUBLE;
        offRight = ballScreenXPixels > CANVAS_WIDTH_PIXELS;

        if (offLeft || offRight)
        {
            cameraOffsetXPixels = golfBall.getPositionXPixels()
                                  - (CANVAS_WIDTH_PIXELS * CAMERA_CENTER_THRESHOLD_RATIO);

            if (cameraOffsetXPixels < INIT_TO_ZERO_DOUBLE)
            {
                cameraOffsetXPixels = INIT_TO_ZERO_DOUBLE;
            }
        }
    }

    private void renderGame()
    {
        graphicsContext.setFill(Color.SKYBLUE);
        graphicsContext.fillRect(INIT_TO_ZERO_DOUBLE, INIT_TO_ZERO_DOUBLE, CANVAS_WIDTH_PIXELS, CANVAS_HEIGHT_PIXELS);

        final List<TerrainTile> terrainTiles;

        terrainTiles = golfCourse.getTerrainTiles();

        terrainTiles.forEach(this::drawTerrainTile);
        golfCourse.getAirObstacles().forEach(this::drawAirObstacle);

        drawBall();

        if (!golfBall.isMoving())
        {
            drawAimArrow();
        }
    }

    private void drawAirObstacle(final AirObstacle airObstacle)
    {
        final double screenLeftXPixels;
        final double screenRightXPixels;

        final double obstacleWidthPixels;
        final double obstacleHeightPixels;

        screenLeftXPixels  = airObstacle.getLeftXPixels() - cameraOffsetXPixels;
        screenRightXPixels = airObstacle.getRightXPixels() - cameraOffsetXPixels;

        obstacleWidthPixels  = screenRightXPixels - screenLeftXPixels;
        obstacleHeightPixels = airObstacle.getBottomYPixels() - airObstacle.getTopYPixels();

        if (screenRightXPixels < INIT_TO_ZERO_DOUBLE || screenLeftXPixels > CANVAS_WIDTH_PIXELS)
        {
            return;
        }

        graphicsContext.setFill(Color.DARKGRAY);
        graphicsContext.fillRect(
            screenLeftXPixels,
            airObstacle.getTopYPixels(),
            obstacleWidthPixels,
            obstacleHeightPixels
                                );

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeRect(
            screenLeftXPixels,
            airObstacle.getTopYPixels(),
            obstacleWidthPixels,
            obstacleHeightPixels
                                  );
    }

    private void drawTerrainTile(final TerrainTile terrainTile)
    {
        final double screenStartXPixels;
        final double screenEndXPixels;
        final double tileWidthOnScreenPixels;

        final double groundCenterYPixels;

        screenStartXPixels      = terrainTile.getStartXPixels() - cameraOffsetXPixels;
        screenEndXPixels        = terrainTile.getEndXPixels() - cameraOffsetXPixels;
        tileWidthOnScreenPixels = screenEndXPixels - screenStartXPixels;

        if (screenEndXPixels < INIT_TO_ZERO_DOUBLE || screenStartXPixels > CANVAS_WIDTH_PIXELS)
        {
            return;
        }

        switch (terrainTile.getTerrainType())
        {
            case FAIRWAY -> graphicsContext.setFill(Color.GREEN);
            case ROUGH -> graphicsContext.setFill(Color.DARKGREEN);
            case SAND -> graphicsContext.setFill(Color.KHAKI);
            case WATER -> graphicsContext.setFill(Color.DEEPSKYBLUE);
            case HOLE, GREEN -> graphicsContext.setFill(Color.LAWNGREEN);
            default -> graphicsContext.setFill(Color.GRAY);
        }

        groundCenterYPixels = terrainTile.getGroundCenterYPixels();

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
        final double flagPoleXPixels;
        final double flagTopYPixels;

        flagPoleXPixels = screenStartXPixels + tileWidthOnScreenPixels * FLAG_POLE_X_MULTIPLIER;
        flagTopYPixels  = groundCenterYPixels - FLAG_HEIGHT_PIXELS;

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(flagPoleXPixels,
                                   groundCenterYPixels,
                                   flagPoleXPixels,
                                   flagTopYPixels);

        graphicsContext.setFill(Color.RED);
        graphicsContext.fillPolygon(
            new double[]{
                flagPoleXPixels,
                flagPoleXPixels + FLAG_TRIANGLE_OFFSET_X_PIXELS,
                flagPoleXPixels
            },
            new double[]{
                flagTopYPixels,
                flagTopYPixels + FLAG_TRIANGLE_OFFSET_Y_SMALL_PIXELS,
                flagTopYPixels + FLAG_TRIANGLE_OFFSET_Y_LARGE_PIXELS
            },
            POLYGON_FILL
                                   );
    }

    private void drawBall()
    {
        final double screenBallXPixels;

        screenBallXPixels = golfBall.getPositionXPixels() - cameraOffsetXPixels;

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillOval(
            screenBallXPixels - BALL_RADIUS_PIXELS,
            golfBall.getPositionYPixels() - BALL_RADIUS_PIXELS,
            BALL_RADIUS_PIXELS * BALL_RADIUS_MULTIPLIER,
            BALL_RADIUS_PIXELS * BALL_RADIUS_MULTIPLIER
                                );

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeOval(
            screenBallXPixels - BALL_RADIUS_PIXELS,
            golfBall.getPositionYPixels() - BALL_RADIUS_PIXELS,
            BALL_RADIUS_PIXELS * BALL_RADIUS_MULTIPLIER,
            BALL_RADIUS_PIXELS * BALL_RADIUS_MULTIPLIER
                                  );
    }

    private void drawAimArrow()
    {
        final double baseArrowXPixels;
        final double baseArrowYPixels;

        final double launchAngleRadians;

        final double normalizedPower;
        final double arrowLengthPixels;

        final double arrowEndXPixels;
        final double arrowEndYPixels;

        final double arrowHeadAngleOffsetRadians;
        final double leftHeadAngleRadians;
        final double rightHeadAngleRadians;

        final double leftHeadXPixels;
        final double leftHeadYPixels;

        final double rightHeadXPixels;
        final double rightHeadYPixels;

        baseArrowXPixels = golfBall.getPositionXPixels() - cameraOffsetXPixels;
        baseArrowYPixels = golfBall.getPositionYPixels();

        launchAngleRadians = Math.toRadians(currentAimAngleDegrees);

        normalizedPower = currentPowerPercentage / MAXIMUM_POWER_PERCENTAGE;

        arrowLengthPixels = AIM_ARROW_BASE_LENGTH_PIXELS + AIM_ARROW_EXTRA_LENGTH_PIXELS * normalizedPower;

        arrowEndXPixels = baseArrowXPixels + arrowLengthPixels * Math.cos(launchAngleRadians);
        arrowEndYPixels = baseArrowYPixels - arrowLengthPixels * Math.sin(launchAngleRadians);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(ARROW_LINE_WIDTH_PIXELS);
        graphicsContext.strokeLine(
            baseArrowXPixels,
            baseArrowYPixels,
            arrowEndXPixels,
            arrowEndYPixels
                                  );

        arrowHeadAngleOffsetRadians = Math.toRadians(AIM_ARROW_HEAD_ANGLE_DEGREES);

        leftHeadAngleRadians  = launchAngleRadians + arrowHeadAngleOffsetRadians;
        rightHeadAngleRadians = launchAngleRadians - arrowHeadAngleOffsetRadians;

        leftHeadXPixels = arrowEndXPixels + AIM_ARROW_HEAD_LENGTH_PIXELS * Math.cos(leftHeadAngleRadians);
        leftHeadYPixels = arrowEndYPixels - AIM_ARROW_HEAD_LENGTH_PIXELS * Math.sin(leftHeadAngleRadians);

        rightHeadXPixels = arrowEndXPixels + AIM_ARROW_HEAD_LENGTH_PIXELS * Math.cos(rightHeadAngleRadians);
        rightHeadYPixels = arrowEndYPixels - AIM_ARROW_HEAD_LENGTH_PIXELS * Math.sin(rightHeadAngleRadians);

        graphicsContext.strokeLine(arrowEndXPixels,
                                   arrowEndYPixels,
                                   leftHeadXPixels,
                                   leftHeadYPixels);

        graphicsContext.strokeLine(arrowEndXPixels,
                                   arrowEndYPixels,
                                   rightHeadXPixels,
                                   rightHeadYPixels);
    }

    private String buildParAndScoreText()
    {
        final String bestRoundText;
        final long sandTileCount;

        final String sandSummary;
        final String holeLabel;

        final int totalParSoFar;
        final int totalStrokesSoFar;
        final int relativeToParSoFar;

        final String roundScoreSummary;

        if (bestRoundRelativeToPar == null)
        {
            bestRoundText = "Best round: none";
        }
        else
        {
            bestRoundText = "Best round: " + formatRelativeToPar(bestRoundRelativeToPar);
        }

        sandTileCount = golfCourse.getTerrainTiles().stream()
                                  .filter(tile -> tile.getTerrainType() == TerrainType.SAND)
                                  .count();

        sandSummary = "Sand tiles: " + sandTileCount;

        holeLabel = "Hole " + (currentHoleIndex + NEXT_HOLE_INDEX) + "/" + NUMBER_OF_HOLES_PER_ROUND;

        totalParSoFar      = computeTotalParUpToCurrentHole();
        totalStrokesSoFar  = computeTotalStrokesUpToCurrentHole();
        relativeToParSoFar = totalStrokesSoFar - totalParSoFar;

        roundScoreSummary = "Round: " + totalStrokesSoFar + "/" + totalParSoFar
                            + " (" + formatRelativeToPar(relativeToParSoFar) + ")";

        return holeLabel
               + " | Par: " + parForHole
               + " | Strokes this hole: " + strokesTakenCount
               + " | " + roundScoreSummary
               + " | " + bestRoundText
               + " | " + sandSummary;
    }

    private void updateParAndScoreLabel()
    {
        if (parAndScoreLabel != null)
        {
            parAndScoreLabel.setText(buildParAndScoreText());
        }
    }
}
