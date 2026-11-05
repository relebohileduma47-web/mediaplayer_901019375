package com.example.assign2;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class JavaFXMediaPlayerMain extends Application {

    private final PlaylistManager playlistManager = new PlaylistManager();
    private final PlaybackController playbackController = new PlaybackController();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("main-tabs");

        Tab homeTab = new Tab("⌂  Home");
        Tab playerTab = new Tab("▶  Player");
        playerTab.setContent(createPlayerWorkspace(primaryStage));
        homeTab.setContent(createHomeView(primaryStage, tabPane, playerTab));
        tabPane.getTabs().addAll(homeTab, playerTab);
        root.setCenter(tabPane);

        FlowPane controlBox = createPlaybackControls();
        root.setBottom(controlBox);

        KeyboardController keyboardController = new KeyboardController(
                playbackController,
                playlistManager,
                this::playNextAuto
        );

        Scene scene = new Scene(root, 1100, 760);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Error: /style.css not found in root resources folder.");
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyboardController::handleKeyPress);
        primaryStage.setTitle("JavaFX Musique");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(560);
        primaryStage.show();
        root.requestFocus();
    }

    private VBox createHomeView(Stage stage, TabPane tabPane, Tab playerTab) {
        Label trackCount = new Label();
        trackCount.getStyleClass().add("stat-value");

        Label audioCount = new Label();
        audioCount.getStyleClass().add("stat-value");

        Label statusValue = new Label("Ready");
        statusValue.getStyleClass().addAll("stat-value", "status-value");

        VBox recentTracks = new VBox(8);
        recentTracks.getStyleClass().add("recent-list");

        Runnable refreshHome = () -> {
            int total = playlistManager.getPlaylist().size();
            trackCount.setText(String.valueOf(total));
            audioCount.setText(total == 0 ? "—" : String.valueOf(total));
            recentTracks.getChildren().clear();
            int start = Math.max(0, total - 4);
            for (int i = start; i < total; i++) {
                Label track = new Label("♫  " + playlistManager.getPlaylist().get(i).getName());
                track.getStyleClass().add("recent-track");
                track.setMaxWidth(Double.MAX_VALUE);
                recentTracks.getChildren().add(track);
            }
            if (recentTracks.getChildren().isEmpty()) {
                Label empty = new Label("Your recent tracks will appear here.");
                empty.getStyleClass().add("muted-copy");
                recentTracks.getChildren().add(empty);
            }
        };
        playlistManager.getListView().getItems().addListener((ListChangeListener<String>) change -> refreshHome.run());
        refreshHome.run();

        Button addMedia = new Button("＋  Add media");
        addMedia.getStyleClass().add("accent-button");
        addMedia.setOnAction(e -> {
            playlistManager.addFiles(stage);
            if (playbackController.getCurrentIndex() == -1 && !playlistManager.getPlaylist().isEmpty()) {
                playMediaAtIndex(0);
            }
            tabPane.getSelectionModel().select(playerTab);
        });

        Button openPlayer = new Button("Open player  →");
        openPlayer.getStyleClass().add("ghost-button");
        openPlayer.setOnAction(e -> tabPane.getSelectionModel().select(playerTab));

        VBox heroCopy = new VBox(14,
                makeEyebrow("YOUR SOUND, YOUR SPACE"),
                label("Good music starts here.", "hero-title"),
                label("Build a personal listening room for every track, idea, and late-night mood.", "hero-copy"),
                new HBox(12, addMedia, openPlayer)
        );
        heroCopy.setMaxWidth(520);

        StackPane hero = new StackPane(heroCopy);
        hero.getStyleClass().add("hero-card");
        hero.setPadding(new Insets(34, 38, 34, 38));
        hero.setMinHeight(245);
        StackPane.setAlignment(heroCopy, Pos.CENTER_LEFT);

        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        stats.add(createStatCard("LIBRARY", trackCount, "tracks saved"), 0, 0);
        stats.add(createStatCard("AUDIO READY", audioCount, "ready to play"), 1, 0);
        stats.add(createStatCard("STATUS", statusValue, "your player"), 2, 0);
        for (int i = 0; i < 3; i++) {
            GridPane.setHgrow(stats.getChildren().get(i), Priority.ALWAYS);
        }

        Label recentTitle = label("Recently added", "section-title");
        Label recentHint = label("The newest pieces in your library", "section-hint");
        VBox recentHeader = new VBox(3, recentTitle, recentHint);
        VBox recentCard = new VBox(16, recentHeader, recentTracks);
        recentCard.getStyleClass().add("content-card");
        recentCard.setPadding(new Insets(22));
        HBox.setHgrow(recentCard, Priority.ALWAYS);

        Label tipTitle = label("Make it yours", "section-title");
        Label tipCopy = label("Drop in a few files and let Musique become your private soundtrack.", "muted-copy");
        VBox tipCard = new VBox(10, label("✦  QUICK NOTE", "card-eyebrow"), tipTitle, tipCopy);
        tipCard.getStyleClass().add("tip-card");
        tipCard.setPadding(new Insets(22));
        tipCard.setPrefWidth(260);

        HBox lower = new HBox(14, recentCard, tipCard);
        VBox content = new VBox(24, hero, stats, lower);
        content.setPadding(new Insets(28, 34, 34, 34));
        content.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("home-scroll");
        return new VBox(scrollPane);
    }

    private VBox createStatCard(String eyebrow, Label value, String caption) {
        Label eyebrowLabel = label(eyebrow, "card-eyebrow");
        Label captionLabel = label(caption, "stat-caption");
        VBox card = new VBox(7, eyebrowLabel, value, captionLabel);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(18));
        card.setMinWidth(150);
        return card;
    }

    private BorderPane createPlayerWorkspace(Stage primaryStage) {
        StackPane videoWrapper = new StackPane();
        videoWrapper.getChildren().addAll(playbackController.getMediaView(), playbackController.getDiskView());
        videoWrapper.setId("video-container");
        videoWrapper.setPadding(new Insets(20));
        videoWrapper.setMinHeight(0);
        videoWrapper.setMinWidth(0);
        playbackController.getMediaView().fitWidthProperty().bind(videoWrapper.widthProperty().subtract(40));
        playbackController.getMediaView().fitHeightProperty().bind(videoWrapper.heightProperty().subtract(40));
        playbackController.getMediaView().setPreserveRatio(true);
        Button addButton = new Button("＋  Add Files");
        Button removeButton = new Button("▱  Remove");
        addButton.setOnAction(e -> {
            playlistManager.addFiles(primaryStage);
            if (playbackController.getCurrentIndex() == -1 && !playlistManager.getPlaylist().isEmpty()) {
                playMediaAtIndex(0);
            }
        });
        removeButton.setOnAction(e -> {
            int newIndex = playlistManager.removeSelectedFile(playbackController.getCurrentIndex());
            if (newIndex == -1) {
                playbackController.stopMedia();
                playbackController.setCurrentIndex(-1);
            } else {
                playbackController.setCurrentIndex(newIndex);
            }
        });
        playlistManager.getListView().getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int selected = newVal.intValue();
            if (selected >= 0 && selected < playlistManager.getPlaylist().size() && selected != playbackController.getCurrentIndex()) {
                playMediaAtIndex(selected);
            }
        });
        VBox.setVgrow(playlistManager.getListView(), Priority.ALWAYS);

        FlowPane playlistControls = new FlowPane(12, 12, addButton, removeButton);
        playlistControls.setAlignment(Pos.CENTER);
        playlistControls.setPadding(new Insets(15, 0, 0, 0));
        Label playlistLabel = new Label("Your Library");
        playlistLabel.getStyleClass().add("sidebar-title");
        VBox playlistBox = new VBox(playlistLabel, playlistManager.getListView(), playlistControls);
        playlistBox.setId("sidebar");
        playlistBox.setPadding(new Insets(25, 20, 25, 20));
        playlistBox.setPrefWidth(280);
        playlistBox.setMinWidth(Region.USE_PREF_SIZE);

        HBox mainArea = new HBox(videoWrapper, playlistBox);
        HBox.setHgrow(videoWrapper, Priority.ALWAYS);
        BorderPane playerRoot = new BorderPane();
        playerRoot.setCenter(mainArea);
        return playerRoot;
    }

    private FlowPane createPlaybackControls() {
        Button playButton = new Button("▶  Play");
        Button pauseButton = new Button("⏸  Pause");
        Button stopButton = new Button("⏹  Stop");
        playButton.getStyleClass().add("play-button");
        playButton.setOnAction(e -> playbackController.playMedia(playlistManager.getPlaylist(), this::playNextAuto));
        pauseButton.setOnAction(e -> playbackController.pauseMedia());
        stopButton.setOnAction(e -> playbackController.stopMedia());

        FlowPane controlBox = new FlowPane(25, 15, playButton, pauseButton, stopButton);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setId("control-bar");
        controlBox.setPadding(new Insets(16));
        return controlBox;
    }

    private Label makeEyebrow(String text) {
        return label(text, "hero-eyebrow");
    }

    private Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    private void playMediaAtIndex(int index) {
        playbackController.selectMedia(index, playlistManager.getPlaylist(), this::playNextAuto);
        playlistManager.getListView().getSelectionModel().select(index);
    }

    private void playNextAuto() {
        playbackController.playNext(playlistManager.getPlaylist(), this::playNextAuto);
        playlistManager.getListView().getSelectionModel().select(playbackController.getCurrentIndex());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
