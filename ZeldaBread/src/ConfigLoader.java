/**
 * @file    ConfigLoader.java
 * @brief   Loads and provides access to game configuration from a JSON file.
 * Uses basic string parsing; assumes a specific JSON structure.
 *
 * @author  Gemini
 * @version 1.0.0
 * @date    2025-04-15
 *
 * @copyright Copyright (c) 2025 Jack Schulte & Gemini. All rights reserved.
 * Strictly confidential and proprietary. Distribution, reproduction,
 * or modification is strictly prohibited without prior written permission.
 */

 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.StandardCharsets;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Objects;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ConfigLoader {
 
     private static final String CONFIG_FILE_PATH = "/config/rpg-config.json"; // Path within resources/classpath
     private static Map<String, Map<String, String>> roomSettings = new HashMap<>();
     private static boolean loaded = false;
 
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
                 jsonContent.append(line.trim());
             }
 
             // Basic parsing - THIS IS FRAGILE AND ASSUMES THE EXACT STRUCTURE
             parseRoomSettings(jsonContent.toString());
             loaded = true;
             System.out.println("Config loaded successfully.");
 
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
 
     // Very basic parser - not robust, highly format-dependent
     private static void parseRoomSettings(String json) {
         // Pattern to find "ROOM_TYPE": { ... } blocks
         Pattern roomBlockPattern = Pattern.compile("\"([A-Z_]+)\"\\s*:\\s*\\{([^}]*)\\}");
         Matcher roomBlockMatcher = roomBlockPattern.matcher(json);
 
         while (roomBlockMatcher.find()) {
             String roomTypeName = roomBlockMatcher.group(1);
             String roomData = roomBlockMatcher.group(2);
             Map<String, String> settings = new HashMap<>();
 
             // Pattern to find "key": "value" pairs within the block
             Pattern kvPattern = Pattern.compile("\"([a-z_]+)\"\\s*:\\s*\"?([^,\"]+)\"?");
             Matcher kvMatcher = kvPattern.matcher(roomData);
 
             while (kvMatcher.find()) {
                 String key = kvMatcher.group(1);
                 String value = kvMatcher.group(2);
                 settings.put(key, value);
             }
              if (!settings.isEmpty()) {
                  roomSettings.put(roomTypeName, settings);
                  // System.out.println("Parsed settings for " + roomTypeName + ": " + settings); // Debug
              }
         }
     }
 
     private static void loadDefaults() {
         System.err.println("Loading default room settings.");
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
         loaded = false; // Indicate defaults are used
     }
 
     public static Map<String, String> getSettingsForRoom(String roomTypeName) {
         if (!loaded && roomSettings.isEmpty()) loadDefaults(); // Ensure defaults are loaded if initial load failed
         return roomSettings.getOrDefault(roomTypeName, new HashMap<>()); // Return empty map if type not found
     }
 
     public static String getColor(String roomTypeName, String defaultColor) {
         return getSettingsForRoom(roomTypeName).getOrDefault("color", defaultColor);
     }
 
     public static int getIntSetting(String roomTypeName, String key, int defaultValue) {
         try {
             return Integer.parseInt(getSettingsForRoom(roomTypeName).getOrDefault(key, String.valueOf(defaultValue)));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 }