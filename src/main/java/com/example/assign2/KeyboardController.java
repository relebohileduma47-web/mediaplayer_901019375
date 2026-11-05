package com.example.assign2; // Defines package location for this class

import javafx.scene.input.KeyCode; // Imports KeyCode enum for identifying pressed keys
import javafx.scene.input.KeyEvent; // Imports KeyEvent class representing key press events
import java.io.File; // Imports File class for file handling references
import java.util.List; // Imports List interface for collections of files

public class KeyboardController { // Defines controller class for managing keyboard shortcuts

    private final PlaybackController playbackController; // Field storing reference to playback controller
    private final PlaylistManager playlistManager; // Field storing reference to playlist manager
    private final Runnable onFinishedCallback; // Field storing callback action for when track finishes

    public KeyboardController(PlaybackController playbackController, PlaylistManager playlistManager, Runnable onFinishedCallback) { // Constructor accepting required dependencies
        this.playbackController = playbackController; // Assigns passed playback controller parameter to field
        this.playlistManager = playlistManager; // Assigns passed playlist manager parameter to field
        this.onFinishedCallback = onFinishedCallback; // Assigns passed completion callback parameter to field
    } // Closes constructor

    public void handleKeyPress(KeyEvent event) { // Handles key press events triggered by user keyboard interaction
        List<File> playlist = playlistManager.getPlaylist(); // Obtains current list of media files from manager

        switch (event.getCode()) { // Evaluates key code of pressed key
            case SPACE: // Handles Spacebar press
                playbackController.togglePlayPause(playlist, onFinishedCallback); // Toggles playback state between play and pause
                syncSelection(); // Synchronizes visual selection in UI list view with active track index
                event.consume(); // Prevents key event from propagating further in event chain
                break; // Exits switch statement

            case S: // Handles 'S' key press
                playbackController.stopMedia(); // Stops active playback
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            case N: // Handles 'N' key press
                playbackController.playNext(playlist, onFinishedCallback); // Plays next track in playlist
                syncSelection(); // Updates highlighted selection in UI list view
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            case P: // Handles 'P' key press
                playbackController.playPrevious(playlist, onFinishedCallback); // Plays previous track in playlist
                syncSelection(); // Updates highlighted selection in UI list view
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            case UP: // Handles Up Arrow key press
                playbackController.adjustVolume(0.1); // Increases playback volume by 10%
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            case DOWN: // Handles Down Arrow key press
                playbackController.adjustVolume(-0.1); // Decreases playback volume by 10%
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            case M: // Handles 'M' key press
                playbackController.toggleMute(); // Toggles audio mute state
                event.consume(); // Consumes event to prevent further processing
                break; // Exits switch statement

            default: // Handles any other key presses
                break; // Ignores unmapped keys and exits switch statement
        } // Closes switch statement
    } // Closes handleKeyPress method

    private void syncSelection() { // Helper method to sync list view selection with current playing index
        int index = playbackController.getCurrentIndex(); // Retrieves active track index from playback controller
        if (index >= 0 && index < playlistManager.getPlaylist().size()) { // Validates that index falls within valid playlist bounds
            playlistManager.getListView().getSelectionModel().select(index); // Highlights item at calculated index in UI list
        } // Closes if block
    } // Closes syncSelection method
} // Closes KeyboardController class