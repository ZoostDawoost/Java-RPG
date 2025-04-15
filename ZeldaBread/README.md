# Legend of Splenda: Bread of the Wild

## Overview

Legend of Splenda: Bread of the Wild is a 2D, top-down roguelike RPG developed in Java using the Swing framework for the UI. It aims to be a parody of classic adventure games with a unique bread-based theme. Players navigate a procedurally generated dungeon, manage resources, and interact with different room types.

## Features

* **Graphical User Interface:** Built with Java Swing, featuring distinct panels for map display, player status, dialogue, character information, and action buttons.
* **Screen Management:** Handles transitions between welcome screen, character creation, and the main game screen using `CardLayout`.
* **Character Creation:** Allows players to select from multiple classes (Knight, Sentinel, Assassin, Wizard, Caveman), each with unique base stats and thematic names. Randomly generated character images are displayed during selection.
* **Procedural Map Generation:** Creates a unique dungeon layout for each playthrough using a randomized expansion algorithm.
* **Room Variety:** Populates the generated map with different room types based on configurable chances and counts (e.g., Enemy, Shop, Smithy, Treasure, Boss).
* **Turn-Based Movement:** Players navigate the dungeon tile by tile using keyboard arrows or UI buttons (Turn Left, Turn Right, Forward, Backward).
* **Exploration (Fog of War):** The map is initially hidden, revealing itself in a 3x3 area around the player as they explore. A toggle (F8) allows viewing the entire map.
* **Resource Management:** Players manage Health Points (HP) and Energy. Moving consumes energy, and bumping into walls costs both HP and energy.
* **Basic Item System:** Includes base classes for `Item` and `Weapon`, along with an `Inventory` system. Players can equip weapons meeting stat requirements.
* **Configurable Settings:** Game parameters like room generation chances, colors, and sound events are loaded from an external JSON file (`rpg-config.json`).
* **Sound Effects:** Plays configured sounds for specific events, such as entering a shop or bumping into a wall.
* **UI Panels:** Includes dedicated panels for dialogue history, player status bars (HP/Energy), player character image during gameplay, and a dynamic room legend (F9).

## Core Modules & Design

The project follows an object-oriented design pattern:

* **`RPG.java`**: The main application class and central game controller.
    * Initializes the game, board, and players.
    * Contains the `main` method entry point.
    * Handles map generation (`buildMap`, `addEventsToRooms`).
    * Provides static utility methods (e.g., `playAudio`, `getCentralGameInstance`, `getPlayers`).
    * Manages global game state like player turn index and map visibility toggle.
* **`UIManagement.java`**: Manages the entire Swing graphical user interface.
    * Creates the main `JFrame` and organizes panels using `BorderLayout` and `CardLayout`.
    * Handles screen transitions (`showScreen`, `welcomeScreen`, `characterCreatorScreen`, `startGameScreen`).
    * Creates and manages UI components like buttons, labels, and text areas.
    * Sets up key bindings (arrow keys, F8, F9, character creation keys).
    * Updates UI elements based on game state changes (`updateGameStatus`, `addDialogue`).
    * Manages the layout and content of the character creation screen.
* **`Board.java`**: Represents the game map as a 2D grid.
    * Holds a `Room[][]` array.
    * Provides methods to get and set rooms or room types at specific coordinates, with bounds checking.
* **`Room.java`**: Represents a single tile on the game board.
    * Uses an `enum RoomType` to define its function (e.g., `START_ROOM`, `ENEMY_ROOM`, `SHOP`).
    * Tracks its state (`visited`, `explored`).
    * Can hold a list of `Item` objects present in the room.
    * Provides `isTraversable()` based on its type.
* **`Player.java`**: Represents the player character.
    * Stores stats (HP, Energy, Vigor, Strength, etc.), class name, character name, position (`currentPos`), and facing direction (`wayFacing`).
    * Holds an `Inventory` object.
    * Manages equipped `Weapon`.
    * Contains core action logic: `movePlayer`, `turnPlayer`, `exploreAround`, `equipWeapon`, `pickupItemFromRoom`, `useItemFromInventory`.
    * Handles consequences of actions (energy cost, wall bump penalties).
* **`ConfigLoader.java`**: Responsible for loading and providing access to configuration data.
    * Reads settings from `/config/rpg-config.json`.
    * Uses basic parsing (regex and string manipulation) to extract `room_settings` and `sound_mappings`.
    * Provides static methods (`getColor`, `getIntSetting`, `getRandomSoundMapping`) to retrieve configuration values.
    * Includes default values if the config file is missing or invalid.
    * Contains `SoundMapping` inner class to structure sound data.
* **`MapPanel.java`**: A custom `JPanel` responsible for drawing the game map.
    * Reads data from the `Board` and `Player` objects.
    * Uses colors defined in `ConfigLoader` to represent different `RoomType` values.
    * Handles drawing based on `explored` and `visited` status, respecting the `showFullMap` toggle.
    * Draws the player marker and facing indicator.
    * Centers the map drawing within the panel area.
* **Item System:**
    * `Item.java`: Abstract base class for all items. Defines common properties like name, description, cost, and drop rate.
    * `Weapon.java`: Subclass of `Item`, adding weapon-specific stats (damage, scaling, requirements, criticals). Includes `doDamage` calculation logic.
    * `Inventory.java`: Manages a fixed-capacity list of `Item` objects for the player. Handles adding and removing items.
* **UI Panels:**
    * `WelcomePanel.java`: Initial screen with game title, image, and player count selection buttons.
    * `DialoguePanel.java`: Displays scrollable game messages and dialogue.
    * `StatusPanel.java`: Shows player HP and Energy using progress bars, and Score as text.
    * `PlayerPanel.java`: Displays the player's chosen character image during gameplay.
    * `RoomLegendPanel.java`: An overlay panel (toggled by F9) showing a legend of room colors, types, and counts (total/visited).
* **`CharacterClassInfo.java`**: Provides static data storage for character classes.
    * Defines base stats for each class using a nested `BaseStats` class.
    * Stores lists of thematic, randomly selectable names for each class.

## Configuration (`/config/rpg-config.json`)

The game's behavior can be customized via the `rpg-config.json` file, loaded by `ConfigLoader.java`.

* **`room_settings`**: An object containing settings for each `Room.RoomType`.
    * Each room type (e.g., `"SHOP"`, `"ENEMY_ROOM"`) is a key.
    * The value is an object containing properties like:
        * `"color"`: Hex color code (String) used by `MapPanel`.
        * `"event_chance_percent"`: (For `ENEMY_ROOM`) Percentage chance a remaining corridor becomes this type.
        * `"event_divisor"`: (For rarer rooms) Used to calculate the target count (total corridors / divisor).
        * `"event_max_count"`: (For rarer rooms) The maximum number of this room type to place.
* **`sound_mappings`**: An object containing mappings for different sound events.
    * Each sound event type (e.g., `"shop-welcome"`, `"action-owich"`) is a key.
    * The value is an array `[]` of sound objects `{}`.
    * Each sound object defines:
        * `"file"`: The path to the `.wav` file (relative to the classpath, e.g., `"audio/shop-welcome_vctk-vits-p226.wav"`).
        * `"description"`: A brief text description of the sound.
        * `"npc_name"`: The name of the NPC or source associated with the sound (used in dialogue).
    * `ConfigLoader.getRandomSoundMapping(soundType)` is used to select one of these objects randomly when the corresponding event occurs in the game.

## Map Generation Logic

The map generation process in `RPG.java` involves two main phases:

1.  **`buildMap()` - Structure Generation:**
    * Initializes the `Board` with `EMPTY_SPACE` rooms.
    * Places the `START_ROOM` at the center (`MAP_SIZE`/2, `MAP_SIZE`/2).
    * Uses a randomized growth algorithm:
        * Maintains a list (`rL`) of existing room coordinates.
        * Repeatedly picks a random room from the list.
        * Finds valid adjacent empty spaces (`findPossibleExpansionMoves`) that:
            * Are within map bounds (excluding border rows/columns).
            * Are currently `EMPTY_SPACE`.
            * Do *not* form a 2x2 block with three existing rooms (`formsSquareWithExisting`). This prevents solid blocks and encourages more complex layouts.
        * If valid expansion moves exist, picks one randomly, sets its type to `CORRIDOR`, adds it to the room list, and increments the room count.
        * If a room has no valid expansion moves, it's removed from the list.
    * Continues until a target number of rooms (`ROOM_GENERATION_TARGET`) is reached or the list is exhausted.

2.  **`addEventsToRooms()` - Room Type Assignment:**
    * Collects all `CORRIDOR` rooms created in the previous phase, excluding those immediately adjacent to the `START_ROOM`.
    * Shuffles the list of available corridor coordinates randomly.
    * Calculates the target count for rarer room types (`BOSS_ROOM`, `SHOP`, `SMITHY`, etc.) based on settings in `rpg-config.json` (`event_divisor`, `event_max_count`) using the `calculateCount` helper method.
    * Iterates through the shuffled list, converting `CORRIDOR` rooms into these special types (`placeRoomTypeByCoord`) until the calculated count for each type is met, starting with the rarest (Boss).
    * For the remaining `CORRIDOR` rooms:
        * Rolls a random percentage chance (`ran.nextInt(100)`).
        * If the roll is less than the `event_chance_percent` defined for `ENEMY_ROOM` in the config, the room becomes `ENEMY_ROOM`.
        * Otherwise, it becomes a `PLAIN_ROOM`.
    * Finally, ensures the starting room is `START_ROOM` and rooms directly adjacent to it are set to `PLAIN_ROOM`.

## How to Run

(Standard Java execution - Assumes compiled .class files are in a `bin` directory and resources/config are accessible via the classpath)

```bash
# Navigate to the project's root directory (containing src, bin, etc.)
cd /path/to/ZeldaBread

# Compile (if necessary - using VS Code's Java extension handles this)
# javac -d bin -cp bin src/*.java

# Run the main class
java -cp bin RPG

Copyright
Copyright (c) 2025 Jack Schulte. All rights reserved.

Strictly confidential and proprietary. Distribution, reproduction, or modification is strictly prohibited without prior written permission.