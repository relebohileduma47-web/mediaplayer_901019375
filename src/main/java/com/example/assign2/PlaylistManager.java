package com.example.assign2;

import javafx.collections.FXCollections; // Imports FXCollections utility class for observable collections
import javafx.collections.ObservableList; // Imports ObservableList interface for auto-updating UI lists
import javafx.scene.control.ListView; // Imports ListView UI control element
import javafx.stage.FileChooser; // Imports FileChooser dialog window for file browsing
import javafx.stage.Stage; // Imports Stage class representing top-level window context

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager { // Defines class responsible for managing media playlist storage and UI lists

    private final ObservableList<String> displayList = FXCollections.observableArrayList(); // List containing track display names bound to UI
    private final List<File> playlist = new ArrayList<>(); // Underlying list containing actual File instances
    private final ListView<String> listView = new ListView<>(displayList); // UI ListView component initialized with display names
    private final File diskStorageFile = new File("playlist.txt"); // Storage file reference for saving/loading playlist paths

    public PlaylistManager() { // Constructor for PlaylistManager
        loadPlaylistFromDisk();
    } // Closes constructor

    public ListView<String> getListView() { return listView; } // Getter for ListView control element
    public List<File> getPlaylist() { return playlist; } // Getter for underlying list of File objects

    public void addFiles(Stage stage) { // Opens file dialog allowing user to choose and append files
        FileChooser fileChooser = new FileChooser(); // Instantiates file chooser dialog control
        fileChooser.setTitle("Select Media Files"); // Sets title text for system file chooser window
        fileChooser.getExtensionFilters().addAll( // Configures supported file type extensions for dialog filter
                new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3", "*.wav", "*.m4a") // Accepts common video/audio extensions
        ); // Closes extension filter list configuration

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage); // Displays multi-file open dialog and captures selection
        if (selectedFiles != null) { // Checks if user selected one or more files (did not cancel)
            for (File file : selectedFiles) { // Iterates through each file returned by user selection
                playlist.add(file); // Appends file reference into internal playlist list
                displayList.add(file.getName()); // Appends filename string into UI observable display list
            } // Closes file iteration loop
            savePlaylistToDisk(); // Persists updated playlist paths to text file on disk
        } // Closes selectedFiles null check
    } // Closes addFiles method

    public int removeSelectedFile(int currentPlayingIndex) { // Removes currently selected item from UI list view
        int selectedIndex = listView.getSelectionModel().getSelectedIndex(); // Gets row index currently selected in UI
        if (selectedIndex >= 0 && selectedIndex < playlist.size()) { // Validates that a valid row is selected
            playlist.remove(selectedIndex); // Removes file object from underlying list
            displayList.remove(selectedIndex); // Removes file name string from display list
            savePlaylistToDisk(); // Saves updated playlist to file storage on disk

            if (playlist.isEmpty()) { // Checks if playlist became empty after removal
                return -1; // Returns -1 signal to indicate no items remain
            } // Closes empty list check

            if (selectedIndex == currentPlayingIndex) { // Handles case where removed track was active playing track
                return selectedIndex % playlist.size(); // Returns target index to automatically play next item
            } else if (selectedIndex < currentPlayingIndex) { // Handles case where removed item was prior to playing item
                return currentPlayingIndex - 1; // Decrements playing index to account for shifted list indices
            } // Closes index offset adjustment checks
        } // Closes valid selected index check
        return currentPlayingIndex; // Returns current index unchanged if no removal occurred
    } // Closes removeSelectedFile method

    private void savePlaylistToDisk() { // Writes full path of all files in playlist to storage file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(diskStorageFile))) { // Opens BufferedWriter inside try-with-resources
            for (File file : playlist) { // Iterates through all File objects in playlist
                writer.write(file.getAbsolutePath()); // Writes absolute system path of file to disk file
                writer.newLine(); // Writes line separator after each file path entry
            } // Closes iteration loop
        } catch (IOException e) { // Catches I/O exceptions that occur during file writing
            System.err.println("Error saving playlist to disk: " + e.getMessage()); // Prints detailed error message to error console stream
        } // Closes try-catch block
    } // Closes savePlaylistToDisk method

    private void loadPlaylistFromDisk() { // Reads file paths from disk file and restores playlist items
        if (!diskStorageFile.exists()) return; // Exits early if saved playlist storage file does not exist

        try (BufferedReader reader = new BufferedReader(new FileReader(diskStorageFile))) { // Opens BufferedReader inside try-with-resources
            String line; // Declares variable to hold each line read from file
            while ((line = reader.readLine()) != null) { // Reads lines sequentially until reaching end of file
                File file = new File(line); // Recreates File object using path read from current line
                if (file.exists()) { // Verifies file still exists on filesystem before adding
                    playlist.add(file); // Restores existing file into internal playlist list
                    displayList.add(file.getName()); // Restores existing filename string into UI display list
                } // Closes file exists check
            } // Closes while reading loop
        } catch (IOException e) { // Catches I/O exceptions that occur during file reading
            System.err.println("Error loading playlist from disk: " + e.getMessage()); // Prints detailed error message to error console stream
        } // Closes try-catch block
    } // Closes loadPlaylistFromDisk method
} // Closes PlaylistManager class