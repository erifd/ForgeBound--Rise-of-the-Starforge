import pymunk as pm
import pygame as pg
import unity8 as pu
import pymunk.pygame_util as pm_pg_util
import math as m

pg.init()

SCREEN_HEIGHT = 700
SCREEN_WIDTH = 1450

#game window
screen = pg.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
pg.display.set_caption("Adventure")

#pymunk space
space = pm.Space()
static_body = pm_pg_util.DrawOptions(screen)

#clock
FPS = 120
clock = pg.time.Clock()

#game variables


#colors
BG = (135, 206, 250)

#load images


#game loop
run = True
while run:
    
    #clock (for FPS)
    clock.tick(FPS)
    space.step(1 / FPS)

    #fill background
    screen.fill(BG)
    
    #event handler
    for event in pg.event.get():
        if event.type == pg.QUIT:
            run = False

    #update screen
    pg.display.flip()