import json
import zlib
import struct
from typing import Any, Dict

class ForgeboundWorldSave:
    """
    Cross-platform handler for .forgeboundwrld save files.
    Compatible with Java implementation.
    """
    
    MAGIC_HEADER = b'FBWRLD'
    VERSION = 1
    
    @staticmethod
    def save(filepath: str, world_data: Dict[str, Any]) -> bool:
        """Save world data to .forgeboundwrld file"""
        try:
            # Convert to JSON
            json_str = json.dumps(world_data, separators=(',', ':'))
            json_bytes = json_str.encode('utf-8')
            
            # Compress
            compressed = zlib.compress(json_bytes, level=9)
            
            # Create file structure
            with open(filepath, 'wb') as f:
                # Write header
                f.write(ForgeboundWorldSave.MAGIC_HEADER)  # 6 bytes
                f.write(struct.pack('B', ForgeboundWorldSave.VERSION))  # 1 byte
                
                # Write data length (4 bytes, big-endian)
                f.write(struct.pack('>I', len(compressed)))
                
                # Write compressed data
                f.write(compressed)
            
            return True
            
        except Exception as e:
            print(f"Error saving world: {e}")
            return False
    
    @staticmethod
    def load(filepath: str) -> Dict[str, Any] | None:
        """Load world data from .forgeboundwrld file"""
        try:
            with open(filepath, 'rb') as f:
                # Verify header
                header = f.read(6)
                if header != ForgeboundWorldSave.MAGIC_HEADER:
                    print("Invalid file format")
                    return None
                
                # Read version
                version = struct.unpack('B', f.read(1))[0]
                if version != ForgeboundWorldSave.VERSION:
                    print(f"Unsupported version: {version}")
                    return None
                
                # Read data length
                data_length = struct.unpack('>I', f.read(4))[0]
                
                # Read and decompress
                compressed = f.read(data_length)
                json_bytes = zlib.decompress(compressed)
                json_str = json_bytes.decode('utf-8')
                
                return json.loads(json_str)
                
        except FileNotFoundError:
            print(f"Save file not found: {filepath}")
            return None
        except Exception as e:
            print(f"Error loading world: {e}")
            return None


# Example Usage
# if __name__ == "__main__":
#     game_data = {
#         'world_name': 'Forgebound Realm',
#         'terrain': [[0, 0, 1], [0, 1, 1], [1, 1, 2]],
#         'points': 2500,
#         'player_position': {'x': 15, 'y': 23, 'z': 8},
#         'choices': {
#             'tutorial_complete': True,
#             'allied_with': 'forge_guild'
#         },
#         'inventory': ['iron_sword', 'health_potion', 'map_fragment']
#     }
    
#     # Save
#     if ForgeboundWorldSave.save("world.forgeboundwrld", game_data):
#         print("✓ Saved")
    
#     # Load
#     loaded = ForgeboundWorldSave.load("world.forgeboundwrld")
#     if loaded:
#         print(f"✓ Loaded: {loaded['world_name']}")