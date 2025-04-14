# Legend of Splenda: Bread of the Wild - Development Status Review

## Current Implementation (as of 2025-04-14)

The game currently establishes a foundation for a 2D, top-down, tile-based roguelike RPG using Java Swing for the UI.

**Core Features Implemented:**

* **Game Window:** A main JFrame is created with distinct areas for title/subtitle, a central display (switches between text and map), and a bottom area for comments and action buttons.
* **UI Management:** Handles screen transitions (title, player count, character creation, game), button creation/actions, hover effects, and displays basic player info (HP, class).
* **Character Classes:** Four predefined classes (Knight, Sentinel, Assassin, Caveman) exist with distinct starting stats (Vigor, Defense, Strength, Dexterity, Agility, Luck). Players select a class during character creation.
* **Player State:** The `RPG` class tracks player stats, current HP (derived from Vigor), and inventory slots (though inventory management logic isn't fully implemented). Currently supports 1 or 2 players, assigned sequentially.
* **Map Generation:** A 21x21 grid map is procedurally generated using a randomized algorithm that connects rooms, avoiding simple 2x2 squares. The generation starts from the center (10,10).
* **Room Events:** After base generation, rooms are assigned types/events randomly, including standard/difficult enemies, shops, shrines, blacksmiths, treasure, and plain rooms. Boss rooms are placed further from the start.
* **Map Display:** A custom `MapPanel` draws the generated map tile by tile, using different colors for different room types.
* **Movement:** Turn-based movement is implemented. Players can turn left/right and move forward/backward using UI buttons or keyboard arrow keys (Up, Down, Left, Right). Movement changes the player's `currentPos`.
* **Exploration (Fog of War):** The map starts mostly hidden. As the player moves, the current room and its eight immediate neighbors are revealed (`exploredMap` in `RPG.java`). `MapPanel` only draws explored rooms by default.
* **Debug Map Toggle:** Pressing F8 during gameplay toggles the map display between showing only explored areas and showing the entire generated map.
* **Basic Items:** `Weapon.java` defines weapon properties (type, cost, scaling, damage, etc.) and `InvSlot.java` provides a base for inventory slots, but item acquisition and usage are not yet implemented.

## Room Types and Meanings

The map uses different colors to represent the type of room or event within it, as defined in `MapPanel.java` and assigned integer values in `RPG.java`:

* **Black (`COLOR_EMPTY`, Type 0):** Empty space outside the generated dungeon rooms. Also used for unexplored areas in the default map view.
* **Light Gray (`COLOR_NORMAL`, Type 1):** A standard connecting room generated during the initial map build, before specific events are assigned. Might remain this type if no other event is assigned later.
* **Cyan (`COLOR_START`, Type 2):** The starting room for the player. (Note: Boss rooms are also currently assigned Type 2 in `addEventsToRooms`, meaning they will appear the same color unless changed).
* **Red (`COLOR_ENEMY_STD`, Type 3):** Contains a standard enemy.
* **Dark Red (`COLOR_ENEMY_DIFF`, Type 4):** Contains a difficult enemy.
* **Yellow (`COLOR_SHOP`, Type 5):** A market or shop room.
* **Orange (`COLOR_SMITH`, Type 6):** A blacksmith room.
* **Green (`COLOR_TREASURE`, Type 7):** A treasure room.
* **White (`COLOR_PLAIN`, Type 8):** An empty room with no specific event.
* **Blue (`COLOR_SHRINE`, Type 9):** A shrine room.
* **Pink (`COLOR_PLAYER`):** Not a room type, but the color of the marker indicating the player's current position. Also includes a black line indicating facing direction.
* **Dark Gray (`COLOR_UNEXPLORED` / Border):** Used for borders around rooms and potentially as a distinct "fog of war" color (though currently unexplored areas default to `COLOR_EMPTY`).

## Scoring / Progression

* **No Explicit Scoring:** Currently, there is no code indicating a point-based scoring system (e.g., no `score` or `experience` variables being tracked or displayed).
* **Progression Placeholder:** Progression seems tied to exploration and survival (maintaining HP). Reaching specific room types (Boss, Shrine, Shop, Treasure) can be considered progression milestones, but the interactions within these rooms are not yet implemented. Stats like Strength, Dexterity, etc., exist but there's no leveling or stat increase mechanism visible yet.

## Features Yet to Be Implemented (Inferred)

Based on the code and the author's thematic description, several key areas need development:

* **Combat System:** Although enemy rooms (Types 3 & 4) are generated, there is no logic for initiating or resolving combat. Damage calculation exists within `Weapon.java` but isn't used.
* **NPC Interaction:** The character "Graindalf" and the tutorial sequence mentioned by the author are not present in the code. Interactions in Shop, Shrine, or Blacksmith rooms are also undefined.
* **Inventory Management:** While `InvSlot` and `Weapon` classes exist, the player cannot currently pick up, drop, equip, or use items/weapons. The initial inventory slots are empty.
* **Parody/Thematic Elements:** Beyond the title ("Legend of Splenda: Bread of the Wild"), specific thematic elements related to Zelda parody or "bread" are not yet integrated into gameplay mechanics, descriptions, or visuals.
* **Objectives/Win Condition:** There is no defined goal or win condition. Reaching boss rooms is possible, but the outcome is not defined.
* **Event Logic:** The specific mechanics for Treasure, Shrine, Shop, Blacksmith, or Enemy encounters need to be coded. What happens when the player enters these rooms?.
* **Player Death/Game Over:** There's no handling for player HP reaching zero.

The project has a solid structural base for a roguelike, but the core gameplay loops (combat, interaction, item use, objectives) and thematic elements are the next major areas for development.