package App;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.*;

public class ForgeboundWorldSave {
    private static final byte[] MAGIC_HEADER = "FBWRLD".getBytes(StandardCharsets.UTF_8);
    private static final byte VERSION = 1;
    
    /**
     * Save world data to a .forgeboundwrld file
     * Pass a Map<String, Object> with your game data
     */
    public static boolean save(String filepath, Map<String, Object> worldData) {
        try (FileOutputStream fos = new FileOutputStream(filepath)) {
            // Convert to JSON manually
            String jsonStr = mapToJson(worldData);
            byte[] jsonBytes = jsonStr.getBytes(StandardCharsets.UTF_8);
            
            // Compress
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(jsonBytes);
            deflater.finish();
            
            byte[] compressed = new byte[jsonBytes.length * 2];
            int compressedLength = deflater.deflate(compressed);
            deflater.end();
            
            // Write header
            fos.write(MAGIC_HEADER);
            fos.write(VERSION);
            
            // Write data length (big-endian)
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(compressedLength);
            fos.write(buffer.array());
            
            // Write compressed data
            fos.write(compressed, 0, compressedLength);
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving world: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load world data from a .forgeboundwrld file
     * Returns a Map<String, Object> with your game data
     */
    public static Map<String, Object> load(String filepath) {
        try (FileInputStream fis = new FileInputStream(filepath)) {
            // Verify header
            byte[] header = new byte[6];
            if (fis.read(header) != 6 || !Arrays.equals(header, MAGIC_HEADER)) {
                System.err.println("Invalid file format");
                return null;
            }
            
            // Read version
            int version = fis.read();
            if (version != VERSION) {
                System.err.println("Unsupported version: " + version);
                return null;
            }
            
            // Read data length
            byte[] lengthBytes = new byte[4];
            fis.read(lengthBytes);
            int dataLength = ByteBuffer.wrap(lengthBytes).getInt();
            
            // Read compressed data
            byte[] compressed = new byte[dataLength];
            fis.read(compressed);
            
            // Decompress
            Inflater inflater = new Inflater();
            inflater.setInput(compressed);
            
            byte[] jsonBytes = new byte[dataLength * 10];
            int jsonLength = inflater.inflate(jsonBytes);
            inflater.end();
            
            // Parse JSON
            String jsonStr = new String(jsonBytes, 0, jsonLength, StandardCharsets.UTF_8);
            return jsonToMap(jsonStr);
            
        } catch (Exception e) {
            System.err.println("Error loading world: " + e.getMessage());
            return null;
        }
    }
    
    // Simple JSON converter - converts Map to JSON string
    private static String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(objectToJson(entry.getValue()));
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    private static String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return mapToJson((Map<String, Object>) obj);
        if (obj instanceof List) return listToJson((List<?>) obj);
        if (obj instanceof int[][]) return array2DToJson((int[][]) obj);
        return "\"" + obj.toString() + "\"";
    }
    
    private static String listToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append(objectToJson(list.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private static String array2DToJson(int[][] array) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) json.append(",");
            json.append("[");
            for (int j = 0; j < array[i].length; j++) {
                if (j > 0) json.append(",");
                json.append(array[i][j]);
            }
            json.append("]");
        }
        json.append("]");
        return json.toString();
    }
    
    // Simple JSON parser - converts JSON string to Map
    private static Map<String, Object> jsonToMap(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return null;
        
        Map<String, Object> map = new HashMap<>();
        json = json.substring(1, json.length() - 1);
        
        int depth = 0;
        int start = 0;
        String currentKey = null;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;
            
            if (depth == 0 && (c == ',' || i == json.length() - 1)) {
                String pair = json.substring(start, i == json.length() - 1 ? i + 1 : i).trim();
                if (!pair.isEmpty()) {
                    int colonIndex = pair.indexOf(':');
                    if (colonIndex > 0) {
                        String key = pair.substring(0, colonIndex).trim().replaceAll("\"", "");
                        String value = pair.substring(colonIndex + 1).trim();
                        map.put(key, parseJsonValue(value));
                    }
                }
                start = i + 1;
            }
        }
        
        return map;
    }
    
    private static Object parseJsonValue(String value) {
        value = value.trim();
        if (value.equals("null")) return null;
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        if (value.startsWith("\"") && value.endsWith("\"")) 
            return value.substring(1, value.length() - 1);
        if (value.startsWith("{")) return jsonToMap(value);
        if (value.startsWith("[")) return parseJsonArray(value);
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    private static List<Object> parseJsonArray(String json) {
        List<Object> list = new ArrayList<>();
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return list;
        
        int depth = 0;
        int start = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;
            
            if (depth == 0 && (c == ',' || i == json.length() - 1)) {
                String item = json.substring(start, i == json.length() - 1 ? i + 1 : i).trim();
                if (!item.isEmpty()) {
                    list.add(parseJsonValue(item));
                }
                start = i + 1;
            }
        }
        
        return list;
    }
    
    // Example usage
    public static void main(String[] args) {
        // Create game data
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("world_name", "Forgebound Realm");
        gameData.put("points", 2500);
        
        Map<String, Integer> position = new HashMap<>();
        position.put("x", 15);
        position.put("y", 23);
        position.put("z", 8);
        gameData.put("player_position", position);
        
        Map<String, Object> choices = new HashMap<>();
        choices.put("tutorial_complete", true);
        choices.put("allied_with", "forge_guild");
        gameData.put("choices", choices);
        
        List<String> inventory = Arrays.asList("iron_sword", "health_potion", "map");
        gameData.put("inventory", inventory);
        
        int[][] terrain = {{0, 0, 1}, {0, 1, 1}, {1, 1, 2}};
        gameData.put("terrain", terrain);
        
        // Save
        if (save("world.forgeboundwrld", gameData)) {
            System.out.println("✓ Saved");
        }
        
        // Load
        Map<String, Object> loaded = load("world.forgeboundwrld");
        if (loaded != null) {
            System.out.println("✓ Loaded: " + loaded.get("world_name"));
            System.out.println("  Points: " + loaded.get("points"));
            System.out.println("  Position: " + loaded.get("player_position"));
        }
    }
}