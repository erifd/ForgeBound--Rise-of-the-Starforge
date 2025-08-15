import torch
import random

# Terrain types
TERRAIN = ['block', 'lava', 'spike', 'tall_block', 'enemy']

# Parameters
SEQUENCE_LENGTH = 20

def generate_level():
    level = [''] * SEQUENCE_LENGTH
    count = 0

    # Step 1: Place 3 consecutive blocks
    start = random.randint(0, SEQUENCE_LENGTH - 3)
    for i in range(3):
        level[start + i] = 'block'
        count += 1

    # Step 2: Place a tall block group (3+ together)
    while True:
        start = random.randint(0, SEQUENCE_LENGTH - 3)
        if all(cell == '' for cell in level[start:start + 3]):
            for i in range(3):
                level[start + i] = 'tall_block'
            break

    # Step 3: Place lava with block on each side
    placed_lava = False
    for i in range(1, SEQUENCE_LENGTH - 1):
        if level[i - 1] == '' and level[i] == '' and level[i + 1] == '':
            level[i - 1] = 'block'
            level[i] = 'lava'
            level[i + 1] = 'block'
            count += 2  # 2 new blocks placed
            placed_lava = True
            break

    # Step 4: Place spikes (up to 4 in a row)
    num_spikes = random.randint(1, 4)
    while True:
        start = random.randint(0, SEQUENCE_LENGTH - num_spikes)
        if all(cell == '' for cell in level[start:start + num_spikes]):
            for i in range(num_spikes):
                level[start + i] = 'spike'
            break

    # Step 5: Fill remaining cells with blocks
    for i in range(SEQUENCE_LENGTH):
        if level[i] == '':
            level[i] = 'block'
            count += 1

    # Step 6: Insert enemies based on block count (1 enemy per 3 blocks)
    max_enemies = count // 3
    enemy_count = 0
    attempts = 0
    max_attempts = 100

    while enemy_count < max_enemies and attempts < max_attempts:
        pos = random.randint(3, SEQUENCE_LENGTH - 2)  # Avoid first 3 positions and edges
        if (level[pos] == 'block' and
            level[pos - 1] == 'block' and
            level[pos + 1] == 'block'):
            level[pos] = 'enemy'
            enemy_count += 1
        attempts += 1

    return level

# Example usage
level = generate_level()
print(level)
