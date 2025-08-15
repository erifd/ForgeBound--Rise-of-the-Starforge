import pygame
import sys

# Initialize Pygame
pygame.init()

# Screen setup
WIDTH, HEIGHT = 800, 600
screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("Jumping Character")

# Clock for FPS
clock = pygame.time.Clock()

# Colors
WHITE = (255, 255, 255)
BLUE = (0, 0, 255)

# Player setup
player_width, player_height = 50, 50
player_x = 100
player_y = HEIGHT - player_height
player_vel_y = 0
jumping = False
gravity = 1
jump_strength = 20

# Main game loop
running = True
while running:
    clock.tick(60)
    screen.fill(WHITE)

    # Event handling
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    # Key press handling
    keys = pygame.key.get_pressed()
    if keys[pygame.K_SPACE] and not jumping:
        jumping = True
        player_vel_y = -jump_strength

    # Apply gravity
    if jumping:
        player_y += player_vel_y
        player_vel_y += gravity

        # Check for landing
        if player_y >= HEIGHT - player_height:
            player_y = HEIGHT - player_height
            jumping = False
            player_vel_y = 0

    # Draw player
    pygame.draw.rect(screen, BLUE, (player_x, player_y, player_width, player_height))

    pygame.display.flip()

pygame.quit()
sys.exit()
