/**
 * @file    ConfigLoader.java
 * @brief   Loads and provides access to game configuration from a JSON file.
 * Uses basic string parsing; assumes a specific JSON structure. Added sound mappings.
 *
 * @author  Gemini
 * @version 1.1.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 *
 * @version History:
 * - 1.1.0 (2025-04-15): Added sound mapping structure, parsing, and retrieval. (Gemini)
 * - 1.0.0 (2025-04-15): Initial version. (Gemini)
 */

 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 public class ConfigLoader {

     private static final String CONFIG_FILE_PATH = "/config/rpg-config.json"; // Path within resources/classpath
     private static Map<String, Map<String, String>> roomSettings = new HashMap<>();
     // *** NEW: Store sound mappings ***
     private static Map<String, List<SoundMapping>> soundMappings = new HashMap<>();
     private static boolean loaded = false;
     private static final Random random = new Random(); // Random instance for sound selection

     // *** NEW: Inner class to hold sound mapping details ***
     public static class SoundMapping {
         public final String file;
         public final String description;
         public final String npcName;

         public SoundMapping(String file, String description, String npcName) {
             this.file = file;
             this.description = description != null ? description : "";
             this.npcName = npcName != null ? npcName : "Unknown NPC";
         }
     }

     // Default values (used if config loading fails)
     private static final Map<String, String> defaultEnemySettings = Map.of("color", "#FF0000", "event_chance_percent", "30");
     private static final Map<String, String> defaultDifficultEnemySettings = Map.of("color", "#8B0000", "event_divisor", "12", "event_max_count", "20");
     private static final Map<String, String> defaultShopSettings = Map.of("color", "#FFFF00", "event_divisor", "25", "event_max_count", "6");
     private static final Map<String, String> defaultSmithySettings = Map.of("color", "#FFA500", "event_divisor", "30", "event_max_count", "4");
     private static final Map<String, String> defaultTreasureSettings = Map.of("color", "#00FF00", "event_divisor", "18", "event_max_count", "10");
     private static final Map<String, String> defaultShrineSettings = Map.of("color", "#0000FF", "event_divisor", "20", "event_max_count", "8");
     private static final Map<String, String> defaultBossSettings = Map.of("color", "#FF00FF", "event_divisor", "60", "event_max_count", "3");
     private static final Map<String, String> defaultStartSettings = Map.of("color", "#00FFFF");
     private static final Map<String, String> defaultPlainSettings = Map.of("color", "#FFFFFF");
     private static final Map<String, String> defaultCorridorSettings = Map.of("color", "#D3D3D3");
     private static final Map<String, String> defaultExploredSettings = Map.of("color", "#696969");

     // *** NEW: Default sound mappings ***
     private static final List<SoundMapping> defaultShopWelcome = List.of(
         new SoundMapping("audio/default-greeting.wav", "A generic greeting.", "Shopkeep")
     );


     static {
         loadConfig();
     }

     private static void loadConfig() {
         System.out.println("Attempting to load config from: " + CONFIG_FILE_PATH);
         try (InputStream is = ConfigLoader.class.getResourceAsStream(CONFIG_FILE_PATH);
              InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is, "Config file not found in classpath"), StandardCharsets.UTF_8);
              BufferedReader reader = new BufferedReader(isr)) {

             StringBuilder jsonContent = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) {
                 jsonContent.append(line); // Keep whitespace for slightly easier parsing if needed
             }

             // Basic parsing - THIS IS FRAGILE AND ASSUMES THE JSON STRUCTURE
             String content = jsonContent.toString();
             parseTopLevel(content); // Call a method to parse different top-level sections

             loaded = true;
             System.out.println("Config loaded successfully.");
             if (soundMappings.isEmpty()) {
                 System.out.println("Warning: No sound mappings found or parsed from config.");
             } else {
                 System.out.println("Parsed sound mappings for types: " + soundMappings.keySet());
             }


         } catch (FileNotFoundException e) {
             System.err.println("ERROR: Config file not found at classpath resource path: " + CONFIG_FILE_PATH);
             loadDefaults();
         } catch (IOException e) {
             System.err.println("ERROR: IOException reading config file: " + e.getMessage());
             loadDefaults();
         } catch (NullPointerException e) {
              System.err.println("ERROR: Could not get resource stream for config file (path correct?): " + CONFIG_FILE_PATH);
              loadDefaults();
         } catch (Exception e) {
             System.err.println("ERROR: Unexpected error loading/parsing config: " + e.getMessage());
             e.printStackTrace(); // Show stack trace for parsing errors
             loadDefaults();
         }
     }

     // Parses the top-level elements like "room_settings" and "sound_mappings"
     private static void parseTopLevel(String json) {
         // Simple extraction based on known keys - assumes keys appear only once at top level
         String roomSettingsJson = extractJsonObject(json, "room_settings");
         String soundMappingsJson = extractJsonObject(json, "sound_mappings");

         if (roomSettingsJson != null) {
             parseRoomSettings(roomSettingsJson);
         } else {
              System.err.println("Warning: 'room_settings' not found in config.");
         }

         if (soundMappingsJson != null) {
             parseSoundMappings(soundMappingsJson); // *** Call new parser ***
         } else {
             System.err.println("Warning: 'sound_mappings' not found in config.");
         }
     }

     // Extracts a JSON object string based on its key - Very basic!
     private static String extractJsonObject(String json, String key) {
         String keyPattern = "\"" + key + "\"\\s*:\\s*\\{";
         Pattern p = Pattern.compile(keyPattern);
         Matcher m = p.matcher(json);
         if (m.find()) {
             int start = m.end() -1; // Start of the object '{'
             int braceCount = 1;
             int end = -1;
             for (int i = start + 1; i < json.length(); i++) {
                 char c = json.charAt(i);
                 if (c == '{') {
                     braceCount++;
                 } else if (c == '}') {
                     braceCount--;
                     if (braceCount == 0) {
                         end = i;
                         break;
                     }
                 }
             }
             if (end != -1) {
                 return json.substring(start, end + 1);
             }
         }
         return null;
     }


     // Parses the "room_settings" block
     private static void parseRoomSettings(String roomSettingsJson) {
         // Pattern to find "ROOM_TYPE": { ... } blocks within the room_settings object
         Pattern roomBlockPattern = Pattern.compile("\"([A-Z_]+)\"\\s*:\\s*\\{([^}]*)\\}");
         Matcher roomBlockMatcher = roomBlockPattern.matcher(roomSettingsJson);

         while (roomBlockMatcher.find()) {
             String roomTypeName = roomBlockMatcher.group(1);
             String roomData = roomBlockMatcher.group(2);
             Map<String, String> settings = new HashMap<>();

             // Pattern to find "key": "value" or "key": number pairs within the block
             // Allows quoted or unquoted numbers for values
             Pattern kvPattern = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"?([^,\"]+)\"?");
             Matcher kvMatcher = kvPattern.matcher(roomData);

             while (kvMatcher.find()) {
                 String key = kvMatcher.group(1);
                 String value = kvMatcher.group(2);
                 // Clean up potential trailing quote if number wasn't quoted
                 if (value.endsWith("\"")) value = value.substring(0, value.length()-1);
                 settings.put(key, value);
             }
              if (!settings.isEmpty()) {
                  roomSettings.put(roomTypeName, settings);
                  // System.out.println("Parsed Room Settings for " + roomTypeName + ": " + settings); // Debug
              }
         }
     }

     // *** NEW: Parses the "sound_mappings" block ***
     private static void parseSoundMappings(String soundMappingsJson) {
         // Pattern to find "sound-type": [ ... ] blocks
         Pattern soundTypePattern = Pattern.compile("\"([a-z\\-]+)\"\\s*:\\s*\\[([^\\]]*)\\]");
         Matcher soundTypeMatcher = soundTypePattern.matcher(soundMappingsJson);

         while (soundTypeMatcher.find()) {
             String soundTypeName = soundTypeMatcher.group(1);
             String soundListData = soundTypeMatcher.group(2); // Content inside the array brackets []

             List<SoundMapping> mappings = new ArrayList<>();

             // Pattern to find individual { ... } objects within the array
             Pattern soundObjectPattern = Pattern.compile("\\{([^}]*)\\}");
             Matcher soundObjectMatcher = soundObjectPattern.matcher(soundListData);

             while (soundObjectMatcher.find()) {
                 String soundObjectData = soundObjectMatcher.group(1); // Content inside {}

                 // Extract file, description, npc_name using regex within the object data
                 String file = extractStringValue(soundObjectData, "file");
                 String description = extractStringValue(soundObjectData, "description");
                 String npcName = extractStringValue(soundObjectData, "npc_name");

                 if (file != null) { // File is mandatory
                     mappings.add(new SoundMapping(file, description, npcName));
                 } else {
                      System.err.println("Warning: Skipping sound mapping object due to missing 'file' field in config for type '" + soundTypeName + "'");
                 }
             }

             if (!mappings.isEmpty()) {
                 soundMappings.put(soundTypeName, mappings);
                 // System.out.println("Parsed Sound Mappings for " + soundTypeName + ": " + mappings.size() + " entries."); // Debug
             }
         }
     }

     // Helper to extract string value for a given key within a JSON object string snippet
     private static String extractStringValue(String jsonData, String key) {
         // Pattern: "key": "value" (captures value)
         Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
         Matcher m = p.matcher(jsonData);
         if (m.find()) {
             return m.group(1);
         }
         return null; // Key or value not found/not string
     }

     // Loads default settings if config file fails
     private static void loadDefaults() {
         System.err.println("Loading default settings (room and sound).");
         roomSettings.clear();
         roomSettings.put("ENEMY_ROOM", new HashMap<>(defaultEnemySettings));
         roomSettings.put("DIFFICULT_ENEMY_ROOM", new HashMap<>(defaultDifficultEnemySettings));
         roomSettings.put("SHOP", new HashMap<>(defaultShopSettings));
         roomSettings.put("SMITHY", new HashMap<>(defaultSmithySettings));
         roomSettings.put("TREASURE_ROOM", new HashMap<>(defaultTreasureSettings));
         roomSettings.put("SHRINE", new HashMap<>(defaultShrineSettings));
         roomSettings.put("BOSS_ROOM", new HashMap<>(defaultBossSettings));
         roomSettings.put("START_ROOM", new HashMap<>(defaultStartSettings));
         roomSettings.put("PLAIN_ROOM", new HashMap<>(defaultPlainSettings));
         roomSettings.put("CORRIDOR", new HashMap<>(defaultCorridorSettings));
         roomSettings.put("EXPLORED_NEUTRAL", new HashMap<>(defaultExploredSettings));

         // *** NEW: Load default sound mappings ***
         soundMappings.clear();
         soundMappings.put("shop-welcome", new ArrayList<>(defaultShopWelcome));

         loaded = false; // Indicate defaults are used
     }

     // --- Public Accessors ---

     public static Map<String, String> getSettingsForRoom(String roomTypeName) {
         if (!loaded && roomSettings.isEmpty() && soundMappings.isEmpty()) loadDefaults();
         return roomSettings.getOrDefault(roomTypeName, Collections.emptyMap()); // Return empty map if type not found
     }

     public static String getColor(String roomTypeName, String defaultColor) {
         return getSettingsForRoom(roomTypeName).getOrDefault("color", defaultColor);
     }

     public static int getIntSetting(String roomTypeName, String key, int defaultValue) {
         String valueStr = getSettingsForRoom(roomTypeName).get(key);
         if (valueStr != null) {
              try {
                  return Integer.parseInt(valueStr);
              } catch (NumberFormatException e) {
                   System.err.println("Warning: Invalid integer format for '" + key + "' in room '" + roomTypeName + "'. Using default.");
                   return defaultValue;
              }
         }
         return defaultValue;
     }

     // *** NEW: Get a random sound mapping for a given type ***
     /**
      * Gets a random SoundMapping object for the specified sound type.
      *
      * @param soundType The type of sound (e.g., "shop-welcome").
      * @return A randomly selected SoundMapping object, or null if the type is not found or has no mappings.
      */
     public static SoundMapping getRandomSoundMapping(String soundType) {
         if (!loaded && roomSettings.isEmpty() && soundMappings.isEmpty()) loadDefaults(); // Ensure loaded

         List<SoundMapping> mappingsForType = soundMappings.get(soundType);

         if (mappingsForType == null || mappingsForType.isEmpty()) {
             // System.err.println("Warning: No sound mappings found for type: " + soundType);
             // Optionally, fallback to a global default sound? For now, return null.
             return null;
         }

         // Select a random mapping from the list
         int randomIndex = random.nextInt(mappingsForType.size());
         return mappingsForType.get(randomIndex);
     }
 }