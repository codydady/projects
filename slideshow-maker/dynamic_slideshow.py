# master slideshow alleviating the need for static compiled imovie videos . super flexible
# author : sriram , date : march 2025

import os
import random
import sqlite3
from PIL import Image, ImageDraw, ImageFont
import pygame
import time
from PIL.ExifTags import TAGS
import psutil
from datetime import datetime
from collections import defaultdict
import atexit

# Define base and relative paths
# this is for mac
BASE_PATH = "/Users/sriram/Desktop/yard/"

DB_PATH = os.path.join(BASE_PATH, "rest/bin/database/temples.db")  # Relative to base

# this is for ubuntu which is normally connected to the sony tv.
IMAGE_DURATION = 2 # Duration to display each image (in seconds)
SLIDESHOW_START_COUNT = 1  # starting index for idol images , to continue from last shown image

# SONG_FOLDER = os.path.join(BASE_PATH, "songs/main_devotional")  # Relative to base
FONT_PATH_TAMIZH = os.path.join(BASE_PATH, "rest/bin/slideshow-maker/nototamir.ttf")  # Relative to base
FONT_PATH_ENGLISH = os.path.join(BASE_PATH, "rest/bin/slideshow-maker/karla.ttf")       

# Baseline thresholds (adjust based on your system)
GOOD_BASELINE = {
    'cpu_percent': 30,    # Below 30% = excellent
    'memory_percent': 50,  # Below 50% = healthy
    'temperature': 70      # Below 70°C = safe (if available)
}
    
# Handle category keys
category_map = {
    pygame.K_a: 'ambal',
    pygame.K_h: 'hanuman',
    pygame.K_m: 'murugan',
    pygame.K_g: 'pillaiyar',
    pygame.K_s: 'shivan' , 
    pygame.K_o: 'others'
}

def get_system_load():
    """Returns current system metrics with performance assessment"""
    metrics = {
        'timestamp': datetime.now().strftime("%H:%M:%S"),
        'cpu': psutil.cpu_percent(interval=1),
        'memory': psutil.virtual_memory().percent,
        'status': "OK"
    }

    # Add temperature if available (Linux/macOS)
    try:
        metrics['temp'] = psutil.sensors_temperatures().get('cpu_thermal', [{}])[0].get('current', 'N/A')
    except:
        metrics['temp'] = 'N/A'
    
    # Performance assessment
    if metrics['cpu'] > GOOD_BASELINE['cpu_percent'] * 1.5:
        metrics['status'] = "HIGH CPU"
    if metrics['memory'] > GOOD_BASELINE['memory_percent']:
        metrics['status'] = "HIGH MEM"
    
    return metrics

def log_load_interval(interval=5):
    """Prints system load every N seconds"""
    last_log = time.time()
    while True:
        if time.time() - last_log >= interval:
            load = get_system_load()
            print(
                f"[{load['timestamp']}] CPU: {load['cpu']:5.1f}% | "
                f"MEM: {load['memory']:5.1f}% | "
                f"TEMP: {load['temp']:4}°C | "
                f"STATUS: {load['status']}"
            )
            last_log = time.time()
        yield  # Allows cooperative multitasking

# as of writing this code image_folder must have sub folders for this program to work - todo-
        # "image_folder": os.path.join(BASE_PATH, "idolsort"), # this is for testing the idol images , change to "temples" for real slideshow

MODES = {
    "temples": {
        "title" : "temples",
        "image_folder": os.path.join(BASE_PATH, "temples"), 
        "song_folder": os.path.join(BASE_PATH, "songs/main_devotional"),
        # Separate cache keys for different resources
        "_cached_image_dirs": None,
        "_cached_song_files": None,  # Changed from _cached_music_dirs
        # Separate getter functions
        "image_dirs": lambda self: get_cached_dirs(self, "_cached_image_dirs", "image_folder"),
        "songs": lambda self: get_cached_songs(self)  # New getter for song files
    },
    "trains": {
        "title" : "trains",
        "image_folder": os.path.join(BASE_PATH, "pics/railway-stations"),
        "song_folder": os.path.join(BASE_PATH, "songs/carnatic"),
        # Separate cache keys for different resources
        "_cached_image_dirs": None,
        "_cached_song_files": None,  # Changed from _cached_music_dirs
        # Separate getter functions
        "image_dirs": lambda self: get_cached_dirs(self, "_cached_image_dirs", "image_folder"),
        "songs": lambda self: get_cached_songs(self)  # New getter for song files
    },
    "personal": {
        "title" : "personal",
        "image_folder": os.path.join(BASE_PATH, "pics"),
        "song_folder": os.path.join(BASE_PATH, "songs/old_songs"),
        # Separate cache keys for different resources
        "_cached_image_dirs": None,
        "_cached_song_files": None,  # Changed from _cached_music_dirs
        # Separate getter functions
        "image_dirs": lambda self: get_cached_dirs(self, "_cached_image_dirs", "image_folder"),
        "songs": lambda self: get_cached_songs(self)  # New getter for song files
    }
}

# Global file handler (opened in append mode)
file_handler = open("rename_commands.txt", "a")

# Register cleanup to close file on program exit
def cleanup():
    if not file_handler.closed:
        file_handler.close()
    print("rename file safely closed.")

# this is to safely tie the file close operation to any form of exit 
atexit.register(cleanup)


def get_all_songs():
    """Returns a consolidated list of all songs from all modes"""
    all_songs = []
    for mode_name, mode_data in MODES.items():
        try:
            # Get songs for current mode
            mode_songs = mode_data["songs"](mode_data)
            if mode_songs:
                all_songs.extend(mode_songs)
        except KeyError:
            print(f"Warning: Mode '{mode_name}' missing song list")
    return all_songs


def get_cached_songs(mode_dict):
    """Recursively find all music files and cache the results"""
    if mode_dict["_cached_song_files"] is None:
        song_files = []
        for root, _, files in os.walk(mode_dict["song_folder"]):
            for file in files:
                if file.lower().endswith((".mp3", ".wav", ".ogg", ".m4a")):  # Add formats as needed
                    song_files.append(os.path.join(root, file))
        mode_dict["_cached_song_files"] = song_files
    return mode_dict["_cached_song_files"]

def get_cached_dirs(mode_dict, cache_key, folder_key):
    """Generic caching function for both images and music"""
    if mode_dict[cache_key] is None:
        mode_dict[cache_key] = get_folders_for_current_mode(mode_dict[folder_key])
    return mode_dict[cache_key]

# Function to get a list of valid folders with their paths and IDs
def get_folders_for_current_mode(root_path):
    folders = []
    for root, dirs, files in os.walk(root_path):
        for folder in dirs:
            folders.append(os.path.join(root, folder))
    return folders

# Usage:
current_mode = MODES["temples"]

# Transition effect options
TRANSITION_FADE = "fade"
TRANSITION_SLIDE_LEFT = "slide_left"
TRANSITION_SLIDE_RIGHT = "slide_right"
TRANSITION_ZOOM = "zoom"
TRANSITION_RANDOM = "random"  # Randomly select an effect

# Set the desired transition effect here
SELECTED_TRANSITION = TRANSITION_FADE  # Change this to any of the above options

# Toggle variable for mode switching
sequential_mode = False  # Default to random mode
image_folders = []
music_folders = []
idol_images_selected = False

# Global dictionary to store indices for each idol
image_indices = {}

# Add these global variables at the top with your other globals
paused = False  # for stopping music
pause_start_time = 0
accumulated_pause_time = 0
slideshow_start_time = 0  # Tracks when the current slide began

# Usage:
master_song_list = get_all_songs()
# print(f"Found {len(master_song_list)} total songs across all modes {master_song_list}")

current_folder_images = []  # Track images in the current folder for sequential mode
current_folder_index = 0  # Track the current image index in sequential mode
static_text = "www.templepages.com"  # Replace with your desired static text
current_song = "hei babi song"

# Function to fetch attributes from SQLite database
def fetch_attributes(db_path, folder_id):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT LOWER(name), LOWER(visit_dt), LOWER(place), LOWER(nearby_town),distance, LOWER(tags) , tam_name , tam_place FROM temples WHERE dm=?", (folder_id,))
    result = cursor.fetchone()
    conn.close()
    return result  # Returns (name, deity, place) or None if not found

def overlay_text(image, text_items, line_spacing=1.2):
    try :
        draw = ImageDraw.Draw(image)
    except Exception as e:
        print(f"Error drawing text in overlay_text : {e}")
        return image  # Return original image if drawing fails

    # Calculate font size based on image height
    font_size_ratio = 0.02
    font_size = int(image.height * font_size_ratio)
    
    # Load the two different fonts globally (using assumed global paths)
    try:
        font_a = ImageFont.truetype(FONT_PATH_TAMIZH, font_size)  # Font A (for the first line)
        font_b = ImageFont.truetype(FONT_PATH_ENGLISH, font_size) # Font B (for subsequent lines)
    except Exception as e:
        print(f"Error loading fonts: {e}. Defaulting to basic font.")
        # Fallback in case of error
        font_a = ImageFont.load_default()
        font_b = ImageFont.load_default()

    # Determine which font to use for calculating the bounding box
    # We must calculate the total height and max width based on the correct font for each line.
    
    # Calculate starting position (70% of the image height)
    start_x = int(image.width * 0.05)  # 5% from the left
    start_y = int(image.height * 0.84)  # 84% from the top
    
    # Calculate total text height and max line width
    total_text_height = 0
    max_line_width = 0

    for index, item in enumerate(text_items):
        # Use font_a for the first line (index 0), and font_b for all others
        current_font = font_a if index == 0 else font_b
        
        # Get bounding box for each item using the correct font
        bbox = draw.textbbox((0, 0), item, font=current_font) 
        text_width = bbox[2] - bbox[0]  # Calculate width (right - left)
        text_height = bbox[3] - bbox[1]  # Calculate height (bottom - top)
        # print (f"for {item} (Font {'A' if index == 0 else 'B'}), text width is {text_width}")
    
        # Use the height of the current line with spacing to calculate total height
        total_text_height += int(text_height * line_spacing)
        
        if text_width > max_line_width:
            max_line_width = text_width  # Update max line width

    # Adjust total height back for the last line which shouldn't have line_spacing applied to its *total* height
    # Subtracting one line_spacing factor and adding back a normal height is complex, 
    # a simpler way for the background is to use the initial height calculation and just use a safe bottom padding
    
    # Define padding for dynamic text
    top_padding = 10
    bottom_padding = 40  # Increase bottom padding (This is where you adjust for cumulative spacing)
    left_padding = 10
    right_padding = 10

    # Calculate background box coordinates for dynamic text
    # Note: total_text_height is slightly over-estimated due to the last line's spacing factor, 
    # but the generous bottom_padding compensates for the safe background box.
    bg_box_coords = [
        (start_x - left_padding, start_y - top_padding),  # Top-left corner
        (start_x + max_line_width + right_padding, start_y + total_text_height + bottom_padding)  # Bottom-right corner
    ]

    # Generate a random lighter background color for dynamic text
    bg_color = get_random_light_color()
    
    # Draw the background box for dynamic text
    draw.rectangle(bg_box_coords, fill=bg_color)
    
    # Overlay the dynamic text items line by line (foreground color is black)
    current_y = start_y
    for index, item in enumerate(text_items):
        # Select the correct font again for drawing
        current_font = font_a if index == 0 else font_b
        
        # Get bounding box to calculate line height accurately
        # Note: We use (start_x, current_y) for the drawing position, but the bbox from (0,0) is for size.
        bbox = draw.textbbox((0, 0), item, font=current_font) 
        text_height = bbox[3] - bbox[1]

        draw.text((start_x, current_y), item, font=current_font, fill="black")  # Draw the text
        
        # Move to the next line based on the current line's height and line_spacing
        current_y += int(text_height * line_spacing)

    # --- Static Text Overlays (Keeping your original logic for these) ---
    
    # Overlay static text at 60% height and 70% width
    static_font_size = int(image.height * 0.02)  # Adjust font size for static text
    # Note: Using FONT_PATH for static text as per your original code
    static_font = ImageFont.truetype(FONT_PATH_ENGLISH, static_font_size)
    
    # Calculate position for static text www.templepages.com
    static_x = int(image.width * 0.80)  # 80% from the left
    static_y = int(image.height * 0.75)  # 70% from the top
    # Draw the static text
    draw.text((static_x, static_y), static_text, font=static_font, fill="gray")  # Gray text

    # position for current song
    static_x = int(image.width * 0.80)  # 80% from the left
    static_y = int(image.height * 0.73)  # 73% from the top
    draw.text((static_x, static_y), current_song, font=static_font, fill="yellow")  # Yellow text

    return image

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

# Function to perform a slide transition between two images
def slide_transition(screen, image1, image2, direction="left", duration=1.0):
    """
    Perform a slide transition between two images.
    
    :param screen: Pygame screen object
    :param image1: First image (Pygame surface)
    :param image2: Second image (Pygame surface)
    :param direction: Direction of the slide ("left", "right", "up", "down")
    :param duration: Duration of the slide effect in seconds (default is 1.0)
    """
    # Ensure both images are the same size as the screen
    image1 = pygame.transform.scale(image1, screen.get_size())
    image2 = pygame.transform.scale(image2, screen.get_size())

    # Slide loop
    start_time = time.time()
    while time.time() - start_time < duration:
        # Calculate the progress (0 to 1)
        progress = (time.time() - start_time) / duration

        # Calculate the offset based on the direction
        if direction == "left":
            offset = int(screen.get_width() * progress)
            screen.blit(image1, (offset, 0))
            screen.blit(image2, (offset - screen.get_width(), 0))
        elif direction == "right":
            offset = int(screen.get_width() * progress)
            screen.blit(image1, (-offset, 0))
            screen.blit(image2, (screen.get_width() - offset, 0))
        elif direction == "up":
            offset = int(screen.get_height() * progress)
            screen.blit(image1, (0, offset))
            screen.blit(image2, (0, offset - screen.get_height()))
        elif direction == "down":
            offset = int(screen.get_height() * progress)
            screen.blit(image1, (0, -offset))
            screen.blit(image2, (0, screen.get_height() - offset))

        # Update the display
        pygame.display.flip()

        # Cap the frame rate
        time.sleep(0.01)

    # Ensure the second image is fully visible at the end
    screen.blit(image2, (0, 0))
    pygame.display.flip()

# Function to perform a zoom transition between two images
def zoom_transition(screen, image1, image2, duration=1.0):
    """
    Perform a zoom transition between two images.
    
    :param screen: Pygame screen object
    :param image1: First image (Pygame surface)
    :param image2: Second image (Pygame surface)
    :param duration: Duration of the zoom effect in seconds (default is 1.0)
    """
    # Ensure both images are the same size as the screen
    image1 = pygame.transform.scale(image1, screen.get_size())
    image2 = pygame.transform.scale(image2, screen.get_size())

    # Zoom loop
    start_time = time.time()
    while time.time() - start_time < duration:
        # Calculate the scale factor (1 to 2)
        scale = 1 + (time.time() - start_time) / duration

        # Scale the first image
        scaled_image1 = pygame.transform.scale(image1, (int(screen.get_width() * scale), int(screen.get_height() * scale)))
        screen.blit(scaled_image1, (screen.get_width() // 2 - scaled_image1.get_width() // 2, screen.get_height() // 2 - scaled_image1.get_height() // 2))

        # Update the display
        pygame.display.flip()

        # Cap the frame rate
        time.sleep(0.01)

    # Ensure the second image is fully visible at the end
    screen.blit(image2, (0, 0))
    pygame.display.flip()

# Function to get a random light color
def get_random_light_color():
    return (random.randint(150, 255), (random.randint(150, 255)), (random.randint(150, 255)))

def get_random_song(song_name=None):
    # all songs are loaded to the song_files variable in the reinitialize_mode method itself - important-
    # If no specific song is requested, return a random song
    chosen_song = random.choice(song_files)
    print("current song is " + os.path.basename(chosen_song))
    return chosen_song

def change_song( song_name=None):
    """Change the currently playing song with proper cleanup and updates"""
    # all songs are loaded to the song_files variable in the reinitialize_mode method itself - important-
    # if choosing a song which is not in the current mode , for eg v key chooses vishnu sahasranamam , it says song not found, 
    # for this we should not allow the song in the other modes but only temples modes

    global current_song
    
    if not song_files:
        print("No songs found in the song folder!")
        return None

    # If a specific song name is provided, try to find it
    if song_name:
        # Ensure the song name has the correct extension
        if not song_name.endswith((".mp3", ".wav")):
            song_name += ".mp3"  # Default to .mp3 if no extension is provided
        print("current song as provided v click is " + song_name)

        # Search for the song in the list . try all 3 lists in mode.... -important-
        for song in master_song_list:
            if os.path.basename(song) == song_name:
                print("current song as provided is " + song)
                current_song = song
        
    else :     
        current_song = get_random_song()

    # do this for both random and chosen songs        
    if current_song:
        pygame.mixer.music.stop()
        pygame.mixer.music.load(current_song)
        pygame.mixer.music.play()  # Play the new song once
        current_song = os.path.splitext(os.path.basename(current_song))[0]

def reinitialize_mode( new_mode ):
    """Reload resources when mode changes"""
    global image_folders, current_mode, song_files, idol_images_selected
    current_mode = MODES[new_mode]
    print(f"switching mode to  '{new_mode}' !")
    # image_folders = get_folders_for_current_mode(MODES[CURRENT_MODE]["image_folder"])
    image_folders =  current_mode["image_dirs"](current_mode)  # Computes once, caches forever
    song_files = current_mode["songs"](current_mode)          # List of song files
    current_folder_images = []
    current_folder_index = 0

    # if an idol was selected , we sud deselect it here
    idol_images_selected = False
    change_song( )

def toggle_music():
    """Toggle pause state without affecting slideshow timing"""
    global paused, pause_start_time
    
    if not pygame.mixer.music.get_busy() and not paused:
        if current_song:
            pygame.mixer.music.play()
    else:
        paused = not paused
        if paused:
            pygame.mixer.music.pause()
            pause_start_time = time.time()
        else:
            pygame.mixer.music.unpause()
            # No timing adjustment needed with new approach

def get_directory_basename(full_path):
    """Extracts the immediate parent directory name from full path"""
    return os.path.basename(os.path.dirname(full_path))

def get_image_date(full_path):
    """Extracts capture date from image EXIF data"""
    try:
        with Image.open(full_path) as img:
            exif_data = img._getexif()
            if exif_data:
                for tag_id, value in exif_data.items():
                    tag = TAGS.get(tag_id, tag_id)
                    if tag == "DateTimeOriginal":
                        return str(value)
    except (AttributeError, IOError, KeyError):
        pass
    return "unknown"  # Fallback if no date found

# Image categories
categorized_images = defaultdict(list)
valid_extensions = ('.jpg', '.jpeg', '.png', '.JPG', '.JPEG', '.PNG')

def load_categorized_images():
    """Load all temple images and categorize by prefix"""
    global categorized_images
    categorized_images.clear()
    
    temples_path = MODES["temples"]["image_folder"]
    for root, _, files in os.walk(temples_path):
        for file in files:
            if file.lower().endswith(valid_extensions) and '_' in file:
                prefix = file.split('_')[0].lower()
                if prefix in ['ambal', 'hanuman', 'murugan', 'others', 'pillaiyar', 'shivan']:   # added others category on may 1, 25 as we need to clean that as well
                    full_path = os.path.join(root, file)
                    categorized_images[prefix].append(full_path)
    
    print("Image categories loaded:")
    for cat, images in categorized_images.items():
        print(f"  {cat}: {len(images)} images")


# this function is only for finding and correcting wrong predicions , this will go away later
# once corrected lots of images will be given back for training the model more efficiently
def rename_command_framer(image_path , new_idol_prefix) : 
    # Example path
    # image_path = "/Users/sriram/Desktop/yard/temples/mahendhirapalli-mhndraplli/ambal_IMG_2018.JPG"
    global file_handler  # Declare as global to ensure proper access

    # Split into directory and filename
    dir_path = os.path.dirname(image_path)
    filename = os.path.basename(image_path)

 # Special case: Remove the existing prefix
    if new_idol_prefix.lower() == 'remove':
        if "_" in filename:
            parts = filename.split("_", 1)  # Split on first underscore only
            new_filename = parts[1]  # Keep only the part after first underscore
            new_path = os.path.join(dir_path, new_filename)
        else:
            new_path = image_path  # No change if no underscore exists
   
    else:       # normal case , replace ONLY the predicted class prefix (e.g., "ambal_" → "pillaiyar_")
        if "_" in filename:
            parts = filename.split("_", 1)  # Split on first underscore only
            new_filename = f"{new_idol_prefix}_{parts[1]}"  # Keep the rest of the filename
            new_path = os.path.join(dir_path, new_filename)
        else:
            new_path = image_path  # No change if no underscore exists

    # if "_" in filename:
    #     parts = filename.split("_", 1)  # Split on first underscore only
    #     new_filename = f"{new_idol_prefix}_{parts[1]}"  # Keep the rest of the filename
    #     new_path = os.path.join(dir_path, new_filename)
    # else:
    #     new_path = image_path  # No change if no underscore exists

    # Generate rename command
    rename_command = f'mv "{image_path}" "{new_path}"'

    # Write to file (instead of executing)
    file_handler.write(rename_command + "\n")

    # print(f"Rename command written to 'rename_commands.txt'")
    print(f"command: {rename_command}")

def get_next_image(selected_idol, categorized_images):
    global image_indices
    # Get current index for selected_idol (default 0 if not set)
    current_index = image_indices.get(selected_idol, SLIDESHOW_START_COUNT)
    # current_index = image_indices.get(selected_idol, 102)  # for testing purpose only , remove later
    # Get image list for selected_idol
    image_list = categorized_images[selected_idol]
    # Fetch image at current index
    image_path = image_list[current_index]
    # Increment index and wrap around to 0 if at end
    currindex = (current_index + 1) % len(image_list)
    image_indices[selected_idol] = currindex
    print(f"showing {currindex}  of  {len(categorized_images[selected_idol])} for {selected_idol}")

    return image_path

# ===============================================
#                                               Main function
# ===============================================

def main():
    global sequential_mode, current_folder_images, current_folder_index, current_song, image_indices
    global paused, image_folders, songs, category_map, current_mode, idol_images_selected

    # Initialize monitoring
    monitor = log_load_interval(5)

    # Initialize pygame for displaying images and playing audio
    pygame.init()
    # pygame.mixer.init()  # was
    pygame.mixer.init(frequency=22050)  # Lower audio quality saves power

    # for full screen 
    screen = pygame.display.set_mode((0, 0), pygame.FULLSCREEN)
    screen = pygame.display.set_mode((0,0), pygame.FULLSCREEN | pygame.HWSURFACE | pygame.DOUBLEBUF)

    # Set your desired window dimensions (width, height)
    # WINDOW_WIDTH = 640  # Change to your preferred width
    # WINDOW_HEIGHT = 480  # Change to your preferred height
    # screen = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT)) 

    pygame.display.set_caption("temple slideshow with devotional songs")

    # Get all valid folders for the default mode which is temples
    reinitialize_mode( "temples")

    # load all idol related images as weall
    load_categorized_images()

    # Main loop to display images dynamically
    running = True
    previous_image = None
    current_folder_path = None  # Track the current folder in sequential mode

    clock = pygame.time.Clock()
    FPS = 20  # Cap frame rate

    while running:
        clock.tick(FPS)  # Prevents 100% CPU usage
        # next(monitor)  # Will print every 5 seconds

        # Check if the current song has finished
        if not pygame.mixer.music.get_busy():
            # Load and play a new random song
            change_song( )
 
         # Get the next image based on the mode
        if sequential_mode:
            # Sequential mode: Exhaust all images in the current folder
            if not current_folder_images or current_folder_index >= len(current_folder_images):
                # Pick a new random folder and reset the image list
                current_folder_path = random.choice(image_folders)
                current_folder_images = [f for f in os.listdir(current_folder_path) if f.endswith((".jpg", ".jpeg", ".JPG", ".JPEG"))]
                current_folder_index = 0

            if current_folder_images:
                selected_image = current_folder_images[current_folder_index]
                image_path = os.path.join(current_folder_path, selected_image)
                current_folder_index += 1  # Move to the next image
            else:
                print(f"No images found in folder: {current_folder_path}")
                continue
        else:
            if not idol_images_selected:
                # print ("random mode idol not selected")
                # Random mode: Pick a random image from a random folder
                current_folder_path = random.choice(image_folders)
                images_in_folder = [f for f in os.listdir(current_folder_path) if f.endswith((".jpg", ".jpeg", ".JPG", ".JPEG"))]
                if images_in_folder:
                    selected_image = random.choice(images_in_folder)
                    image_path = os.path.join(current_folder_path, selected_image)
                    # Update current folder and images for sequential mode
                    current_folder_images = images_in_folder
                    current_folder_index = 0  # Reset index for sequential mode
                else:
                    print(f"No images found in folder: {current_folder_path}")
                    continue
            else:
                # print ("random mode idol ---- selected")
                # idol images must be selected from their respective arrays

                # change this if you want sequential or random image
                # for random file
                # image_path = random.choice( categorized_images[selected_idol] )

                # for sequential load from list
                image_path = get_next_image(selected_idol, categorized_images)

                # current_folder_path sud be set as weall 
                current_folder_path = os.path.dirname(image_path)

        # for all modes , lets display the current chosen image. this helps in debugging as there is some loading fault
        # print(f"current loaded image : {image_path}")

        # attributes are required from the sqlite3 database only for temples mode.
        if ( current_mode["title"]  == "temples" ) :
            parts = current_folder_path.split("-")
            folder_id = parts[-1]
            # print(f"folder id extracted is  {folder_id}")

            # Fetch attributes from the database
            attributes = fetch_attributes(DB_PATH, folder_id)
            if not attributes:
                print(f"No attributes found for folder ID: {folder_id}")
                continue

            name, visit_dt, place, nearby_town, distance, tags , tam_name, tam_place= attributes

            # Open the image and overlay attributes
            # if english temple names are required

            # text_items = [
            #     f"sthalam: {name}, {place}",
            #     # f"near: {nearby_town} @ {distance} kms ; visited - {visit_dt}"
            #     f"near: {nearby_town} @ {distance} kms"
            # ]
            # if tamir temple names are required
            text_items = [
                f"{tam_name}, {tam_place}",
                # f"near: {nearby_town} @ {distance} kms ; visited - {visit_dt}"
                f"near: {nearby_town} @ {distance} kms"
            ]

            if tags is not None and str(tags).strip():  # Check for None and non-empty/non-space strings
                # text_items.append(f"tags: {tags}")
                text_items.append(tags)

        elif ( current_mode["title"]  == "trains" ) :

            # read the image date and containing folder and send it as overlay text
            containing_folder = get_directory_basename(image_path)
            # Example usage:
            date_taken = get_image_date(image_path)  # Returns "2023:05:15 12:30:45"

            text_items = [
                f"route : {containing_folder}",
                f"date : {date_taken} "
            ]
        else : # ( current_mode["title"]  == "personal" ) :

            # read the image date and containing folder and send it as overlay text
            containing_folder = get_directory_basename(image_path)
            # Example usage:
            date_taken = get_image_date(image_path)  # Returns "2023:05:15 12:30:45"

            text_items = [
                f"folder : {containing_folder}",
                f"date : {date_taken} "
            ]

        # flow is common for all modes from here on 
        image = Image.open(image_path)
        image = overlay_text(image, text_items)

        # Convert PIL image to Pygame surface
        current_image = pygame.image.fromstring(image.tobytes(), image.size, image.mode)

        # Apply transition effect if there is a previous image
        if previous_image is not None:
            if SELECTED_TRANSITION == TRANSITION_RANDOM:
                transition = random.choice([TRANSITION_FADE, TRANSITION_SLIDE_LEFT, TRANSITION_SLIDE_RIGHT, TRANSITION_ZOOM])
            else:
                transition = SELECTED_TRANSITION

            if transition == TRANSITION_FADE:
                fade_transition(screen, previous_image, current_image)
            elif transition == TRANSITION_SLIDE_LEFT:
                slide_transition(screen, previous_image, current_image, direction="left")
            elif transition == TRANSITION_SLIDE_RIGHT:
                slide_transition(screen, previous_image, current_image, direction="right")
            elif transition == TRANSITION_ZOOM:
                zoom_transition(screen, previous_image, current_image)
        else:
            # Display the first image without a transition
            display_image(image, screen)

        # Update the previous image
        previous_image = current_image

        # In your display loop:
        slideshow_start_time = time.time()
        remaining_duration = IMAGE_DURATION  # Reset for new slide

        while remaining_duration > 0:
            current_time = time.time()
            
            if not paused:
                # Only decrement timer when not paused
                elapsed = current_time - slideshow_start_time
                remaining_duration = max(0, IMAGE_DURATION - elapsed)

            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False
                    
                elif event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_v:  # Change song when 'c' is pressed
                        change_song( "vishnu_sahasranamam")

                    elif event.key == pygame.K_t:  # Press 'T' for temples
                        reinitialize_mode( "temples")

                    elif event.key == pygame.K_r:  # Press 'R' for trains
                        reinitialize_mode( "trains")

                    elif event.key == pygame.K_p:  # Press 'P' for personal
                        reinitialize_mode( "personal")

                    elif event.key == pygame.K_n:  # Change or next song when 'n' is pressed
                        change_song( )
 
                    elif event.key == pygame.K_1:  # pillaiyar rename
                        print(f"1 key pressed-> rename  : {image_path} to pillaiyar_file")
                        rename_command_framer(image_path , "pillaiyar")

                    elif event.key == pygame.K_2:  # murugan rename
                        print(f"2 key pressed-> rename  : {image_path} to murugan_file")
                        rename_command_framer(image_path , "murugan")

                    elif event.key == pygame.K_3:  # ambal rename
                        print(f"3 key pressed-> rename  : {image_path} to ambal_file")
                        rename_command_framer(image_path , "ambal")

                    elif event.key == pygame.K_4:  # hanuman rename
                        print(f"4 key pressed-> rename  : {image_path} to hanuman_file")
                        rename_command_framer(image_path , "hanuman")

                    elif event.key == pygame.K_5:  # shivan rename
                        print(f"5 key pressed-> rename  : {image_path} to shivan_file")
                        rename_command_framer(image_path , "shivan")

                    elif event.key == pygame.K_6:  # others rename
                        print(f"6 key pressed-> rename  : {image_path} to others_file")
                        rename_command_framer(image_path , "others")

                    elif event.key == pygame.K_7:  # remove the idol rename and take picture back to original name
                        print(f"7 key pressed-> un rename  : {image_path} to file")
                        rename_command_framer(image_path , "remove")

                    elif event.key == pygame.K_SPACE:
                        toggle_music()
                        # When pausing/unpausing, reset the slideshow timer
                        slideshow_start_time = time.time() - (IMAGE_DURATION - remaining_duration)

                    elif event.key in (pygame.K_a, pygame.K_h, pygame.K_m, pygame.K_g, pygame.K_o, pygame.K_s):
                        # set mode to temples as these are temples , it doesnt work properly else
                        reinitialize_mode( "temples")

                        sequential_mode = False  # Default to random mode on normal slideshow

                        selected_idol = category_map[event.key]
                        idol_images_selected = True
                        # also it must be in random order as there is no sequential mode ( directory )
                        print(f"category changed -> current idol : {selected_idol}")

                    elif event.key == pygame.K_q:  # Quit slideshow when 'Q' is pressed
                        running = False

                    elif event.key == pygame.K_f:  # Toggle mode when 'f' is pressed , i.e it is folder after folder or random
                        sequential_mode = not sequential_mode
                        print(f"Sequential mode: {sequential_mode}")
                        # Reset sequential mode tracking when toggling
                        current_folder_images = []
                        current_folder_index = 0

            if not running:
                break

            # Small delay to prevent CPU overuse
            pygame.time.delay(10)

    # Stop the music and quit pygame
    pygame.mixer.music.stop()
    pygame.quit()

if __name__ == "__main__":
    main()
