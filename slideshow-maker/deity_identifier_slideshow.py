# master slideshow alleviating the need for static compiled imovie videos . super flexible
# author : sriram , date : march 2025

import os
from PIL import Image, ImageDraw, ImageFont
from dynamic_slideshow import TRANSITION_FADE
import pygame
import time
from datetime import datetime
from collections import defaultdict

# 1. Pre-init the mixer with standard settings to avoid the "handshake hang"
pygame.mixer.pre_init(44100, -16, 2, 512)

os.environ['SDL_VIDEODRIVER'] = 'cocoa'

# Define base and relative paths
# this is for mac
BASE_PATH = "/Users/sriram/Desktop/yard/"
# this is for quickly fixing the prefixes in loft
DEITY_ID_BASE_PATH = "/Users/sriram/Desktop/loft/"

DB_PATH = os.path.join(BASE_PATH, "rest/bin/database/temples.db")  # Relative to base

# this is for ubuntu which is normally connected to the sony tv.
IMAGE_DURATION = 2 # Duration to display each image (in seconds)
SLIDESHOW_START_COUNT = 1  # starting index for idol images , to continue from last shown image

# SONG_FOLDER = os.path.join(BASE_PATH, "songs/main_devotional")  # Relative to base
FONT_PATH_TAMIZH = os.path.join(BASE_PATH, "rest/bin/slideshow-maker/nototamir.ttf")  # Relative to base
FONT_PATH_ENGLISH = os.path.join(BASE_PATH, "rest/bin/slideshow-maker/karla.ttf")       

# Handle category keys
category_map = {
    pygame.K_a: 'ambal',
    pygame.K_h: 'hanuman',
    pygame.K_m: 'murugan',
    pygame.K_g: 'pillaiyar',
    pygame.K_s: 'shivan' , 
    pygame.K_o: 'others'
}

# as of writing this code image_folder must have sub folders for this program to work - todo-
        # "image_folder": os.path.join(BASE_PATH, "idolsort"), # this is for testing the idol images , change to "temples" for real slideshow
        # "image_folder": os.path.join(BASE_PATH, "temples"), for real slideshow

MODES = {
    "temples": {
        "title" : "temples",
        "image_folder": os.path.join(DEITY_ID_BASE_PATH, "idolsort"), # this is for testing the idol images , change to "temples" for real slideshow
    }
}
image_folders =image_folders = MODES["temples"]["image_folder"]

# Function to get a list of valid folders with their paths and IDs
def get_folders_for_current_mode(root_path):
    folders = []
    for root, dirs, files in os.walk(root_path):
        for folder in dirs:
            folders.append(os.path.join(root, folder))
    return folders

# Usage:
current_mode = MODES["temples"]

# Toggle variable for mode switching
sequential_mode = True  # Default to random mode
image_folders = []
music_folders = []

# Global dictionary to store indices for each idol
image_indices = {}

# Add these global variables at the top with your other globals
paused = False  # for stopping music
pause_start_time = 0
accumulated_pause_time = 0
slideshow_start_time = 0  # Tracks when the current slide began

current_folder_images = []  # Track images in the current folder for sequential mode
current_folder_index = 0  # Track the current image index in sequential mode
static_text = "www.templepages.com"  # Replace with your desired static text
current_song = "hei babi song"

# Function to display a single image
def display_image(image, screen):
    # Convert PIL image to Pygame surface
    image_surface = pygame.image.fromstring(image.tobytes(), image.size, image.mode)

    # Resize the image to fit the screen
    screen_width, screen_height = screen.get_size()
    image_surface = pygame.transform.scale(image_surface, (screen_width, screen_height))

    # Display the image
    screen.blit(image_surface, (0, 0))
    pygame.display.flip()

# Function to perform a fade transition between two images
def fade_transition(screen, image1, image2, duration=1.0):
    """
    Perform a fade transition between two images.
    
    :param screen: Pygame screen object
    :param image1: First image (Pygame surface)
    :param image2: Second image (Pygame surface)
    :param duration: Duration of the fade effect in seconds (default is 1.0)
    """
    # Ensure both images are the same size as the screen
    image1 = pygame.transform.scale(image1, screen.get_size())
    image2 = pygame.transform.scale(image2, screen.get_size())

    # Fade loop
    start_time = time.time()
    while time.time() - start_time < duration:
        # Calculate the alpha value (0 to 255)
        alpha = int(255 * ((time.time() - start_time) / duration))
        if alpha > 255:
            alpha = 255

        # Draw the first image
        screen.blit(image1, (0, 0))

        # Draw the second image with increasing alpha
        image2.set_alpha(alpha)
        screen.blit(image2, (0, 0))

        # Update the display
        pygame.display.flip()

        # Cap the frame rate
        time.sleep(0.01)

    # Ensure the second image is fully visible at the end
    image2.set_alpha(255)
    screen.blit(image2, (0, 0))
    pygame.display.flip()

def rename_file_instantly(image_path, new_idol_prefix):
    """Renames the file on disk immediately and returns the new path."""
    dir_path = os.path.dirname(image_path)
    filename = os.path.basename(image_path)
    
    # 1. Determine the new filename
    if new_idol_prefix.lower() == 'remove':
        if "_" in filename:
            new_filename = filename.split("_", 1)[1]
        else:
            return image_path # Nothing to remove
    else:
        # If it already has a prefix, replace it; otherwise, add it
        base_name = filename.split("_", 1)[1] if "_" in filename else filename
        new_filename = f"{new_idol_prefix}_{base_name}"

    new_path = os.path.join(dir_path, new_filename)

    # 2. Perform the actual disk operation
    try:
        os.rename(image_path, new_path)
        print(f"RENAME SUCCESS: {filename} -> {new_filename}")
        return new_path
    except Exception as e:
        print(f"RENAME FAILED: {e}")
        return image_path

# ===============================================
#                 Main function
# ===============================================
def main():
    global sequential_mode, current_folder_images, current_folder_index
    global paused, image_folders

    pygame.init()
    screen = pygame.display.set_mode((640, 480)) 
    pygame.display.set_caption("Immediate Sorting Mode")

    root_path = MODES["temples"]["image_folder"]
    image_folders = get_folders_for_current_mode(root_path)
    
    running = True
    previous_image = None
    folder_index = 0
    clock = pygame.time.Clock()

    while running:
        clock.tick(20)

        if not current_folder_images or current_folder_index >= len(current_folder_images):
            if folder_index >= len(image_folders):
                break

            current_folder_path = image_folders[folder_index]
            folder_index += 1
            current_folder_images = [f for f in os.listdir(current_folder_path) 
                                     if f.lower().endswith((".jpg", ".jpeg"))]
            current_folder_index = 0
            if not current_folder_images: continue

        selected_filename = current_folder_images[current_folder_index]
        image_path = os.path.join(current_folder_path, selected_filename)
        
        # Display Logic
        image = Image.open(image_path)
        current_image = pygame.image.fromstring(image.tobytes(), image.size, image.mode)
        if previous_image:
            # Note: transition logic here if needed
            display_image(image, screen)
        else:
            display_image(image, screen)

        previous_image = current_image
        slideshow_start_time = time.time()
        remaining_duration = IMAGE_DURATION 

        while remaining_duration > 0:
            if not paused:
                remaining_duration = max(0, IMAGE_DURATION - (time.time() - slideshow_start_time))

            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False
                elif event.type == pygame.KEYDOWN:
                    # Map keys to prefixes
                    prefixes = {
                        pygame.K_1: "pillaiyar", pygame.K_2: "murugan",
                        pygame.K_3: "ambal", pygame.K_4: "hanuman",
                        pygame.K_5: "shivan", pygame.K_6: "others",
                        pygame.K_7: "remove"
                    }
                    
                    if event.key in prefixes:
                        # Rename immediately
                        old_path = image_path
                        image_path = rename_file_instantly(old_path, prefixes[event.key])
                        
                        # UPDATE MEMORY: Replace the name in our current list so 
                        # the index doesn't get messed up if we revisit this folder
                        new_filename = os.path.basename(image_path)
                        current_folder_images[current_folder_index] = new_filename
                        
                    elif event.key == pygame.K_SPACE:
                        paused = not paused
                        slideshow_start_time = time.time() - (IMAGE_DURATION - remaining_duration)
                    elif event.key == pygame.K_q:
                        running = False

            if not running: break
            pygame.time.delay(10)
        
        current_folder_index += 1 # Move to next image AFTER the timer/input loop

    pygame.quit()

if __name__ == "__main__":
    main()