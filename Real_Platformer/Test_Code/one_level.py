import pygame
import sys

pygame.init()

# Constants
SCREEN_WIDTH = 480
SCREEN_HEIGHT = 240
TILE_SIZE = 32
GRAVITY = 1
JUMP_STRENGTH = 15

# Setup display
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
pygame.display.set_caption("Scrolling Platformer")

# New terrain layout (20 tiles)
level = [
    'block', 'lava', 'lava', 'block', 'spike',
    'block', 'enemy', 'block', 'tall_block', 'block',
    'block', 'lava', 'enemy', 'block', 'spike',
    'block', 'tall_block', 'block', 'block', 'block'
]

LEVEL_WIDTH = len(level) * TILE_SIZE

# Surfaces
block_surf = pygame.Surface((TILE_SIZE, TILE_SIZE)); block_surf.fill((100, 100, 255))
lava_surf = pygame.Surface((TILE_SIZE, TILE_SIZE)); lava_surf.fill((255, 50, 50))
spike_surf = pygame.Surface((TILE_SIZE, TILE_SIZE)); spike_surf.fill((200, 200, 200))
tall_block_surf = pygame.Surface((TILE_SIZE, TILE_SIZE * 2)); tall_block_surf.fill((100, 255, 100))
enemy_surf = pygame.Surface((TILE_SIZE, TILE_SIZE)); enemy_surf.fill((255, 0, 255))

# Enemies
enemies = []

def spawn_enemies():
    enemies.clear()  # FIX duplication
    for i, tile in enumerate(level):
        if tile == 'enemy':
            x = i * TILE_SIZE
            y = SCREEN_HEIGHT - TILE_SIZE - TILE_SIZE // 2
            rect = pygame.Rect(x, y, TILE_SIZE, TILE_SIZE)
            enemies.append({'rect': rect, 'dir': 1, 'origin_x': x})

def find_starting_block():
    for i, tile in enumerate(level):
        if tile in ('block', 'tall_block'):
            tile_x = i * TILE_SIZE
            tile_y = SCREEN_HEIGHT - TILE_SIZE
            if tile == 'block':
                return pygame.Rect(tile_x, tile_y - TILE_SIZE, TILE_SIZE, TILE_SIZE)
            elif tile == 'tall_block':
                return pygame.Rect(tile_x, tile_y - TILE_SIZE * 2, TILE_SIZE, TILE_SIZE)

spawn_enemies()
player = find_starting_block()
player_speed = 4
player_vel_y = 0
on_ground = False

# Scroll
scroll_x = 0

# Death & scroll-back flags
dead = False
scrolling_back = False

# Game loop
clock = pygame.time.Clock()
running = True
while running:
    clock.tick(60)
    screen.fill((0, 0, 0))

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    keys = pygame.key.get_pressed()

    if dead:
        scrolling_back = True
        player_vel_y = 0

    if scrolling_back:
        scroll_x -= 10
        scroll_x = max(0, scroll_x)

        if scroll_x == 0:
            player = find_starting_block()
            spawn_enemies()  # now safely resets
            dead = False
            scrolling_back = False
            player_vel_y = 0
    else:
        # Movement
        move_x = 0
        if keys[pygame.K_LEFT]:
            move_x = -player_speed
        if keys[pygame.K_RIGHT]:
            move_x = player_speed

        player.x += move_x

        # Gravity
        player.y += player_vel_y
        player_vel_y += GRAVITY
        on_ground = False

        # Terrain collision
        for i, tile in enumerate(level):
            tile_x = i * TILE_SIZE
            tile_y = SCREEN_HEIGHT - TILE_SIZE

            if tile == 'block':
                tile_rect = pygame.Rect(tile_x, tile_y, TILE_SIZE, TILE_SIZE)
            elif tile == 'tall_block':
                tile_rect = pygame.Rect(tile_x, tile_y - TILE_SIZE, TILE_SIZE, TILE_SIZE * 2)
            else:
                continue

            if player.colliderect(tile_rect):
                if player_vel_y > 0 and player.bottom <= tile_rect.top + player_vel_y:
                    player.bottom = tile_rect.top
                    player_vel_y = 0
                    on_ground = True
                elif player_vel_y < 0 and player.top >= tile_rect.bottom - player_vel_y:
                    player.top = tile_rect.bottom
                    player_vel_y = 0

        # Jump
        if keys[pygame.K_SPACE] and on_ground:
            player_vel_y = -JUMP_STRENGTH

        # Move enemies
        for enemy in enemies:
            enemy['rect'].x += enemy['dir']
            if abs(enemy['rect'].x - enemy['origin_x']) >= TILE_SIZE:
                enemy['dir'] *= -1

        # Collision: danger tiles
        for i, tile in enumerate(level):
            tile_x = i * TILE_SIZE
            tile_y = SCREEN_HEIGHT - TILE_SIZE
            tile_rect = None

            if tile in ('lava', 'spike'):
                tile_rect = pygame.Rect(tile_x, tile_y, TILE_SIZE, TILE_SIZE)

            if tile_rect and player.colliderect(tile_rect):
                dead = True

        # Collision: enemies
        for enemy in enemies:
            if player.colliderect(enemy['rect']):
                dead = True

        # Fall off screen
        if player.y > SCREEN_HEIGHT:
            dead = True

        # Clamp and scroll
        player.x = max(0, min(player.x, LEVEL_WIDTH - TILE_SIZE))
        scroll_x = player.x - SCREEN_WIDTH // 2
        scroll_x = max(0, min(scroll_x, LEVEL_WIDTH - SCREEN_WIDTH))

    # Draw terrain
    for i, tile in enumerate(level):
        tile_x = i * TILE_SIZE - scroll_x
        tile_y = SCREEN_HEIGHT - TILE_SIZE

        if tile_x + TILE_SIZE < 0 or tile_x > SCREEN_WIDTH:
            continue

        if tile == 'block':
            screen.blit(block_surf, (tile_x, tile_y))
        elif tile == 'lava':
            screen.blit(lava_surf, (tile_x, tile_y))
        elif tile == 'spike':
            screen.blit(spike_surf, (tile_x, tile_y))
        elif tile == 'tall_block':
            screen.blit(tall_block_surf, (tile_x, tile_y - TILE_SIZE))

    # Draw enemies
    for enemy in enemies:
        screen.blit(enemy_surf, (enemy['rect'].x - scroll_x, enemy['rect'].y))

    # Draw player
    player_screen_x = player.x - scroll_x
    pygame.draw.rect(screen, (255, 124, 200), (player_screen_x, player.y, TILE_SIZE, TILE_SIZE))
    pygame.display.flip()

pygame.quit()
sys.exit()