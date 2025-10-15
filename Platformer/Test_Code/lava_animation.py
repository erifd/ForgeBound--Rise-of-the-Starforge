import pygame
import random

# Initialize pygame
pygame.init()

# Screen dimensions
WIDTH, HEIGHT = 800, 600
screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("Rising Pixel Bubbles")

# Colors
WHITE = (255, 255, 255)
BUBBLE_COLORS = [(255, 223, 0), (255, 165, 0)]  # yellow & orange shades

# Bubble class
class Bubble:
    def __init__(self):
        self.reset()

    def reset(self):
        self.x = random.randint(0, WIDTH)
        self.y = random.randint(HEIGHT, HEIGHT + 200)
        self.radius = random.randint(3, 6)  # smaller, pixel-like
        self.speed = random.uniform(1, 2)
        self.color = random.choice(BUBBLE_COLORS)

    def update(self):
        self.y -= self.speed
        if self.y + self.radius < 0:
            self.reset()

    def draw(self, surface):
        pygame.draw.circle(surface, self.color, (int(self.x), int(self.y)), self.radius)

# Create 40 bubbles
bubbles = [Bubble() for _ in range(40)]

# Main loop
clock = pygame.time.Clock()
running = True

while running:
    screen.fill(WHITE)  # white background

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    for bubble in bubbles:
        bubble.update()
        bubble.draw(screen)

    pygame.display.flip()
    clock.tick(60)

pygame.quit()
