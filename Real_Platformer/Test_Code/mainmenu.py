import pygame, os

# Initialize Pygame
pygame.init()

# Set up the display
SCREEN_WIDTH, SCREEN_HEIGHT = 10000, 10000
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
FPS = 60
clock = pygame.time.Clock()

# Load the main menu image (image WILL be modified, just pseudo-code, might even only be used in Godot/Unity)
my_folder = os.path.abspath(os.path.dirname(__file__))
pic_location = os.path.join(my_folder, "main_menu_animation.png")
main = pygame.image.load(pic_location)

# Extract frames from the main image (assuming it's a sprite sheet)
main_menu_frames = []
rows, cols = 11, 6  # Adjust based on your image structure
frame_width, frame_height = SCREEN_WIDTH, SCREEN_HEIGHT

for row in range(rows):
    for col in range(cols):
        x_pos = col * frame_width
        y_pos = row * frame_height
        source_rect = pygame.Rect(x_pos, y_pos, frame_width, frame_height)
        main_menu_frames.append(source_rect)

# Animation state
current_frame = 0
frame_speed = 2  # Lower value for faster animation

# Main loop
running = True
while running:
    # FPS control
    clock.tick(FPS)

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    # Clear screen
    screen.fill((0, 0, 0))

    # Iterate through all sections by updating the frame index
    if current_frame < 77:
        current_frame = (current_frame + 1) % (len(main_menu_frames) * frame_speed)
        frame_index = current_frame // frame_speed
    elif current_frame >= 77:
        continue

    # Render the current frame
    screen.blit(main, (0, 0), main_menu_frames[frame_index])

    # Update display
    pygame.display.flip()

# Quit Pygame
pygame.quit()