import pygame
import time
import os

pygame.init()

# Screen setup
screen = pygame.display.set_mode((800, 600))
pygame.display.set_caption("Pixel Font Text Engine Example")

# Colors
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
HIGHLIGHT = (255, 255, 0)

# --- Project root setup ---
script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(script_dir)

def find_file_partial(name_without_ext, search_dir, exts=None):
    if exts is None:
        exts = [".png", ".jpg", ".jpeg", ".bmp", ".gif", ".ttf"]
    for root, dirs, files in os.walk(search_dir):
        for file in files:
            fname, ext = os.path.splitext(file)
            if fname.lower() == name_without_ext.lower() and ext.lower() in exts:
                return os.path.join(root, file)
    return None

# Font
font_path = find_file_partial("Minecraftia-Regular", project_root, [".ttf"])
if not font_path:
    raise FileNotFoundError(f"Could not find 'Minecraftia-Regular.ttf'")
font = pygame.font.Font(font_path, 20)

# Tiny font for "Press Space" message
tiny_font = pygame.font.Font(font_path, 16)
press_space_text = tiny_font.render("Press Space to continue", True, BLACK)
press_space_rect = press_space_text.get_rect(center=(370, 375))
press_blink_on = True
last_press_blink_time = time.time()
press_blink_interval = 0.5
press_show_time = 3  # seconds after line finished
line_finished_time = 0  # track when current line finished

# Text box
bg_file = find_file_partial("textbg", project_root)
if not bg_file:
    raise FileNotFoundError(f"Could not find 'textbg'")
text_bg = pygame.image.load(bg_file).convert_alpha()
text_bg = pygame.transform.scale(text_bg, (800, int(1/3*600)))

# Player token
player_token_path = find_file_partial("playertoken", project_root)
if not player_token_path:
    raise FileNotFoundError(f"Could not find 'playertoken'")
player_token = pygame.image.load(player_token_path).convert_alpha()
player_token = pygame.transform.scale(player_token, (155, 220))

# Dialogue
lines = [
    ("Player", "Hello, welcome to the text engine demo!"),
    ("Player", "This is a demo - hopefully this works"),
    ("Player", "And here’s another line for testing."),
    ("Player", "Adding even more lines to test scrolling in the text box."),
    ("Bob", "Hi! I'm Bob the stickfigure. How's your day going?"),
    ("System", "Answer Bob's Question:"),
    ("System", "This is a system line that should hide the token.")
]

# State
text = 0
displayed_text = ""
text_index = 0
last_update_time = time.time()
letter_delay = 0.05
start_x, start_y = 250, 450
max_x, line_height, bottom_y = 705, 20, 535
max_lines_in_box = (bottom_y - start_y) // line_height + 1
rendered_lines = []

choices = ["Great!", "Meh", "BAD"]
choice_index = 0
choosing = False
chosen = None  # stores selected choice
follow_up_text = ""  # stores response text
follow_up_done = False  # has response been fully displayed
return_index = None  # remembers where to return after choice

blink_on = True
last_blink_time = time.time()
blink_interval = 0.5
cursor_blink_on = True
last_cursor_blink = time.time()
cursor_blink_interval = 0.5

def update_rendered_lines(char):
    global rendered_lines
    if not rendered_lines:
        rendered_lines.append(char)
    else:
        test_line = rendered_lines[-1] + char
        if start_x + font.size(test_line)[0] > max_x:
            rendered_lines.append(char)
        else:
            rendered_lines[-1] = test_line

def draw_text():
    global cursor_blink_on, last_cursor_blink
    if time.time() - last_cursor_blink > cursor_blink_interval:
        cursor_blink_on = not cursor_blink_on
        last_cursor_blink = time.time()

    lines_to_draw = rendered_lines[-max_lines_in_box:]
    for idx, line in enumerate(lines_to_draw):
        screen.blit(font.render(line, False, WHITE), (start_x, start_y + idx * line_height))

    if not choosing and chosen is None and text is not None and text >= 0 and text_index < len(lines[text][1]) and cursor_blink_on:
        if rendered_lines:
            cursor_x = start_x + font.size(rendered_lines[-1])[0]
            cursor_y = start_y + (len(lines_to_draw)-1) * line_height
            pygame.draw.rect(screen, WHITE, (cursor_x, cursor_y, 8, font.get_height()))

def draw_choices():
    global blink_on, last_blink_time
    if time.time() - last_blink_time > blink_interval:
        blink_on = not blink_on
        last_blink_time = time.time()

    for i, option in enumerate(choices):
        color = HIGHLIGHT if (i == choice_index and blink_on) else WHITE
        screen.blit(font.render(option, False, color), (start_x, start_y + i * line_height))

running = True
while running:
    screen.fill(BLACK)

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

        if event.type == pygame.KEYDOWN:
            if event.key == pygame.K_SPACE:
                # Skip normal line typing
                if not choosing and chosen is None and text is not None and text >= 0 and text_index < len(lines[text][1]):
                    displayed_text = lines[text][1]
                    text_index = len(lines[text][1])
                    rendered_lines = []
                    for c in displayed_text:
                        update_rendered_lines(c)
                    line_finished_time = time.time()

                # Skip follow-up response typing
                elif chosen is not None and not follow_up_done:
                    displayed_text = follow_up_text
                    rendered_lines = []
                    for c in displayed_text:
                        update_rendered_lines(c)
                    follow_up_done = True
                    line_finished_time = time.time()

                # Advance after response
                elif chosen is not None and follow_up_done:
                    chosen = None
                    follow_up_done = False
                    displayed_text = ""
                    rendered_lines = []
                    text_index = 0
                    if return_index is not None:
                        text = return_index + 1 if return_index + 1 < len(lines) else -1
                        return_index = None

                # Advance normal lines fully typed
                elif not choosing and chosen is None and text != -1 and text is not None and text_index >= len(lines[text][1]):
                    speaker, line = lines[text]
                    if speaker == "System" and "Answer" in line:
                        choosing = True
                        return_index = text
                    else:
                        text = text + 1 if text < len(lines) - 1 else -1
                        text_index = 0
                        rendered_lines, displayed_text = [], ""
                        line_finished_time = time.time()

            # Navigate choices
            if choosing:
                if event.key == pygame.K_UP:
                    choice_index = (choice_index - 1) % len(choices)
                elif event.key == pygame.K_DOWN:
                    choice_index = (choice_index + 1) % len(choices)
                elif event.key == pygame.K_RETURN:
                    chosen = choices[choice_index]
                    if chosen == "Great!":
                        follow_up_text = "Bob: Glad to hear that!"
                    elif chosen == "Meh":
                        follow_up_text = "Bob: Well, at least it's not bad."
                    else:
                        follow_up_text = "Bob: Oh no! Hope it gets better."
                    choosing = False
                    text = None
                    text_index = 0
                    displayed_text = ""
                    follow_up_done = False
                    line_finished_time = time.time()

    # Typing effect for normal lines
    if not choosing and chosen is None and text is not None and text >= 0:
        if text_index < len(lines[text][1]) and time.time() - last_update_time > letter_delay:
            char = lines[text][1][text_index]
            displayed_text += char
            update_rendered_lines(char)
            text_index += 1
            last_update_time = time.time()
        elif text_index >= len(lines[text][1]) and line_finished_time == 0:
            line_finished_time = time.time()

    # Typing effect for follow-up response
    elif chosen is not None and not follow_up_done:
        if time.time() - last_update_time > letter_delay:
            if text_index < len(follow_up_text):
                char = follow_up_text[text_index]
                displayed_text += char
                # Properly append to rendered_lines
                if not rendered_lines:
                    rendered_lines.append(char)
                else:
                    test_line = rendered_lines[-1] + char
                    if start_x + font.size(test_line)[0] > max_x:
                        rendered_lines.append(char)
                    else:
                        rendered_lines[-1] = test_line
                text_index += 1
            else:
                follow_up_done = True
                line_finished_time = time.time()
            last_update_time = time.time()

    # Draw text box
    screen.blit(text_bg, (0, 400))

    # Draw player token for normal lines only
    if text is not None and text >= 0 and lines[text][0] == "Player" and chosen is None:
        screen.blit(player_token, (85, 385))

    # Draw choices or text
    if choosing:
        draw_choices()
    else:
        draw_text()

    # Draw blinking "Press Space" after 3 seconds of line completion
    if line_finished_time > 0 and time.time() - line_finished_time >= press_show_time:
        if time.time() - last_press_blink_time > press_blink_interval:
            press_blink_on = not press_blink_on
            last_press_blink_time = time.time()
        if press_blink_on:
            screen.blit(press_space_text, press_space_rect)

    pygame.display.flip()

pygame.quit()
