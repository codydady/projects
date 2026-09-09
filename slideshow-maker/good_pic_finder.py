import os
import sys
import json
import pygame
from pathlib import Path


# ============================================================
# CONFIGURATION
# ============================================================

# Root folder containing all your photo folders.
ROOT_FOLDER = Path.home() / "Desktop" / "yard" / "temples"

DELETED_ROOT = Path(
    "/Users/sriram/Desktop/loft/deleted_temples"
)

IMAGE_EXTENSIONS = {
    ".jpg",
    ".jpeg",
    ".png",
    ".webp",
    ".bmp",
    ".gif",
}

GOOD_SUFFIX = "_good"

# Progress file lives BESIDE this Python program.
PROGRAM_FOLDER = Path(__file__).resolve().parent
PROGRESS_FILE = PROGRAM_FOLDER / "good_pic_finder_progress.json"
NO_GOOD_FILE = PROGRAM_FOLDER / "good_pic_finder_no_good_folders.txt"

# ============================================================
# COLORS
# ============================================================

BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GRAY = (150, 150, 150)
DARK_GRAY = (40, 40, 40)
GREEN = (80, 220, 120)
YELLOW = (255, 220, 80)
RED = (240, 80, 80)


# ============================================================
# FILE HELPERS
# ============================================================

def is_image(path: Path) -> bool:
    return (
        path.is_file()
        and path.suffix.lower() in IMAGE_EXTENSIONS
    )


def is_good(path: Path) -> bool:
    """
    Returns True if the filename ends with _good.

    Example:
        temple.jpg       -> False
        temple_good.jpg  -> True
    """

    return path.stem.endswith(GOOD_SUFFIX)


def brand_good(path: Path) -> Path:
    """
    Rename:

        temple.jpg

    to:

        temple_good.jpg
    """

    if is_good(path):
        return path

    new_path = path.with_name(
        path.stem + GOOD_SUFFIX + path.suffix
    )

    if new_path.exists():
        return new_path

    path.rename(new_path)

    return new_path


def move_to_deleted(path: Path) -> Path:
    """
    Move a rejected image to:

        deleted_temples/<original folder name>/<filename>
    """

    folder_name = path.parent.name

    destination_folder = (
        DELETED_ROOT / folder_name
    )

    destination_folder.mkdir(
        parents=True,
        exist_ok=True,
    )

    destination = (
        destination_folder / path.name
    )

    # Avoid accidentally overwriting an existing file.
    if destination.exists():

        counter = 1

        while destination.exists():

            destination = (
                destination_folder
                / f"{path.stem}_{counter}{path.suffix}"
            )

            counter += 1

    path.rename(destination)

    return destination

# ============================================================
# FOLDER / PHOTO DISCOVERY
# ============================================================

def get_folders(root: Path):
    """
    Find all folders recursively that contain images.
    """

    folders = []

    for directory in sorted(root.rglob("*")):

        if not directory.is_dir():
            continue

        images = [
            p
            for p in directory.iterdir()
            if is_image(p)
        ]

        if images:
            folders.append(directory)

    return folders


def get_images(folder: Path):
    """
    Return only images that have not already been branded.
    """

    return [
        p
        for p in sorted(folder.iterdir())
        if is_image(p) and not is_good(p)
    ]

def get_all_images(folder: Path):
    return [
        p
        for p in folder.iterdir()
        if is_image(p)
    ]

# ============================================================
# PROGRESS
# ============================================================

def load_progress():

    if not PROGRESS_FILE.exists():
        return {
            "folder_index": 0,
            "image_index": 0,
            "total_processed": 0,
            "total_good": 0,
            "total_skipped": 0,
            "folders_completed": 0,
        }

    try:

        with open(
            PROGRESS_FILE,
            "r",
            encoding="utf-8",
        ) as f:

            data = json.load(f)

        return {
            "folder_index": data.get(
                "folder_index",
                0,
            ),
            "image_index": data.get(
                "image_index",
                0,
            ),
            "total_processed": data.get(
                "total_processed",
                0,
            ),
            "total_good": data.get(
                "total_good",
                0,
            ),
            "total_skipped": data.get(
                "total_skipped",
                0,
            ),
            "folders_completed": data.get(
                "folders_completed",
                0,
            ),
        }

    except Exception as e:

        print(
            f"Could not read progress file: {e}"
        )

        return {
            "folder_index": 0,
            "image_index": 0,
            "total_processed": 0,
            "total_good": 0,
            "total_skipped": 0,
            "folders_completed": 0,
        }


def save_progress(
    folder_index,
    image_index,
    total_processed,
    total_good,
    total_skipped,
    folders_completed,
):

    data = {
        "folder_index": folder_index,
        "image_index": image_index,
        "total_processed": total_processed,
        "total_good": total_good,
        "total_skipped": total_skipped,
        "folders_completed": folders_completed,
    }

    try:

        with open(
            PROGRESS_FILE,
            "w",
            encoding="utf-8",
        ) as f:

            json.dump(
                data,
                f,
                indent=4,
            )

    except Exception as e:

        print(
            f"Could not save progress: {e}"
        )

def record_no_good_folder(folder: Path):
    """
    Record a folder if no photo in that folder was marked GOOD.

    The folder name is written only once.
    """

    folder_name = folder.name

    existing = set()

    if NO_GOOD_FILE.exists():
        with open(
            NO_GOOD_FILE,
            "r",
            encoding="utf-8",
        ) as f:
            existing = {
                line.strip()
                for line in f
                if line.strip()
            }

    if folder_name not in existing:
        with open(
            NO_GOOD_FILE,
            "a",
            encoding="utf-8",
        ) as f:
            f.write(folder_name + "\n")

        print(
            f"[NO GOOD PHOTOS] {folder_name}"
        )

# ============================================================
# IMAGE DISPLAY
# ============================================================

def load_image(path: Path, screen_size):

    try:

        image = pygame.image.load(
            str(path)
        )

        image = image.convert_alpha()

    except pygame.error as e:

        print(
            f"Could not load image: {path}"
        )

        print(e)

        return None

    screen_width, screen_height = screen_size

    available_width = screen_width
    available_height = screen_height - 100

    image_width, image_height = image.get_size()

    if image_width <= 0 or image_height <= 0:
        return None

    scale = min(
        available_width / image_width,
        available_height / image_height,
    )

    new_width = max(
        1,
        int(image_width * scale),
    )

    new_height = max(
        1,
        int(image_height * scale),
    )

    return pygame.transform.smoothscale(
        image,
        (new_width, new_height),
    )


def draw_centered_image(screen, image):

    if image is None:
        return

    screen_width, screen_height = screen.get_size()

    x = (
        screen_width
        - image.get_width()
    ) // 2

    available_height = screen_height - 100

    y = (
        available_height
        - image.get_height()
    ) // 2

    screen.blit(
        image,
        (x, y),
    )


# ============================================================
# TEXT
# ============================================================

def draw_text(
    screen,
    text,
    font,
    color,
    x,
    y,
    center=False,
):

    surface = font.render(
        text,
        True,
        color,
    )

    if center:

        rect = surface.get_rect(
            center=(x, y)
        )

    else:

        rect = surface.get_rect(
            topleft=(x, y)
        )

    screen.blit(
        surface,
        rect,
    )


# ============================================================
# MAIN
# ============================================================

def main():

    print()
    print("==============================================")
    print("             GOOD PIC FINDER")
    print("==============================================")
    print()

    print(
        f"Root folder : {ROOT_FOLDER}"
    )

    print(
        f"Progress    : {PROGRESS_FILE}"
    )

    print()

    if not ROOT_FOLDER.exists():

        print(
            "ERROR: Root folder does not exist."
        )

        print(ROOT_FOLDER)

        sys.exit(1)

    # --------------------------------------------------------
    # Find folders
    # --------------------------------------------------------

    folders = get_folders(
        ROOT_FOLDER
    )

    total_files = sum(
        len(get_images(folder))
        for folder in folders
    )

    if not folders:

        print(
            "No folders containing images found."
        )

        sys.exit(1)

    print(
        f"Found {len(folders)} folders."
    )

    # --------------------------------------------------------
    # Load saved progress
    # --------------------------------------------------------

    progress = load_progress()

    folder_index = progress[
        "folder_index"
    ]

    image_index = progress[
        "image_index"
    ]

    total_processed = progress[
        "total_processed"
    ]

    current_folder_good = 0

    total_good = progress[
        "total_good"
    ]

    total_skipped = progress[
        "total_skipped"
    ]

    folders_completed = progress[
        "folders_completed"
    ]

    # Make sure saved folder index is valid.

    if folder_index >= len(folders):

        folder_index = 0
        image_index = 0

    # --------------------------------------------------------
    # Pygame
    # --------------------------------------------------------

    pygame.init()

    pygame.display.set_caption(
        "Good Pic Finder"
    )

    screen = pygame.display.set_mode(
        (1400, 900),
        pygame.RESIZABLE,
    )

    clock = pygame.time.Clock()

    font_large = pygame.font.SysFont(
        "Arial",
        28,
    )

    font_medium = pygame.font.SysFont(
        "Arial",
        21,
    )

    font_small = pygame.font.SysFont(
        "Arial",
        17,
    )

    # --------------------------------------------------------
    # Current folder
    # --------------------------------------------------------

    current_images = []

    current_image_path = None

    current_image_surface = None

    def load_folder():

        nonlocal current_images
        nonlocal image_index

        while folder_index < len(folders):

            folder = folders[
                folder_index
            ]

            current_images = get_images(
                folder
            )

            # Saved index might be beyond the
            # current list because files were
            # branded during an earlier run.

            if image_index >= len(
                current_images
            ):

                image_index = 0

            if current_images:

                return True

            return False

        return False

    load_folder()

    # --------------------------------------------------------
    # Main loop
    # --------------------------------------------------------

    running = True

    all_done = False

    while running:

        # ----------------------------------------------------
        # Advance folder if necessary
        # ----------------------------------------------------

        if (
            not all_done
            and image_index >= len(
                current_images
            )
        ):

            if current_images:

                folders_completed += 1

                if current_folder_good == 0:
                    record_no_good_folder(
                        folders[folder_index]
                    )
                    
            folder_index += 1
            current_folder_good = 0
            image_index = 0

            if folder_index >= len(
                folders
            ):

                all_done = True

                save_progress(
                    folder_index,
                    image_index,
                    total_processed,
                    total_good,
                    total_skipped,
                    folders_completed,
                )

            else:

                load_folder()

            current_image_path = None

            current_image_surface = None

        # ----------------------------------------------------
        # Load current photo
        # ----------------------------------------------------

        if (
            not all_done
            and current_image_path is None
            and image_index < len(
                current_images
            )
        ):

            current_image_path = (
                current_images[
                    image_index
                ]
            )
            print(
                f"[CURRENT] {current_image_path.name}"
            )

            current_image_surface = (
                load_image(
                    current_image_path,
                    screen.get_size(),
                )
            )

        # ----------------------------------------------------
        # Events
        # ----------------------------------------------------

        for event in pygame.event.get():

            if event.type == pygame.QUIT:

                running = False

            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    # Trackpad tap / mouse click = NEXT
                    if current_image_path is not None:

                        total_skipped += 1
                        total_processed += 1

                        image_index += 1

                        save_progress(
                            folder_index,
                            image_index,
                            total_processed,
                            total_good,
                            total_skipped,
                            folders_completed,
                        )

                        current_image_path = None
                        current_image_surface = None

            elif event.type == pygame.KEYDOWN:

                # --------------------------------------------
                # ESC
                # --------------------------------------------

                if event.key == pygame.K_ESCAPE:

                    save_progress(
                        folder_index,
                        image_index,
                        total_processed,
                        total_good,
                        total_skipped,
                        folders_completed,
                    )

                    running = False

                # --------------------------------------------
                # G = GOOD
                # --------------------------------------------

                elif (
                    event.key == pygame.K_g
                    and current_image_path
                    is not None
                ):

                    try:

                        new_path = brand_good(
                            current_image_path
                        )

                        total_good += 1
                        total_processed += 1
                        current_folder_good += 1

                        print(
                            f"[GOOD] "
                            f"{current_image_path.name}"
                            f" -> "
                            f"{new_path.name}"
                        )

                    except Exception as e:

                        print(
                            f"ERROR branding "
                            f"{current_image_path}: "
                            f"{e}"
                        )

                    image_index += 1

                    save_progress(
                        folder_index,
                        image_index,
                        total_processed,
                        total_good,
                        total_skipped,
                        folders_completed,
                    )

                    current_image_path = None

                    current_image_surface = None


                # --------------------------------------------
                # D = DELETE

                elif (
                    event.key == pygame.K_d
                    and current_image_path is not None
                ):

                    try:

                        destination = move_to_deleted(
                            current_image_path
                        )

                        print(
                            f"[DELETED] "
                            f"{current_image_path.name}"
                            f" -> "
                            f"{destination}"
                        )

                        total_processed += 1

                        image_index += 1

                        save_progress(
                            folder_index,
                            image_index,
                            total_processed,
                            total_good,
                            total_skipped,
                            folders_completed,
                        )

                        current_image_path = None
                        current_image_surface = None

                    except Exception as e:

                        print(
                            f"ERROR moving "
                            f"{current_image_path}: {e}"
                        )
                        
                # --------------------------------------------
                # SPACE = NEXT
                # --------------------------------------------

                elif (
                    event.key == pygame.K_SPACE
                    and current_image_path
                    is not None
                ):

                    print(
                        f"[SKIP] "
                        f"{current_image_path.name}"
                    )

                    total_skipped += 1
                    total_processed += 1

                    image_index += 1

                    save_progress(
                        folder_index,
                        image_index,
                        total_processed,
                        total_good,
                        total_skipped,
                        folders_completed,
                    )

                    current_image_path = None

                    current_image_surface = None

                # --------------------------------------------
                # RIGHT = NEXT
                # --------------------------------------------

                elif (
                    event.key == pygame.K_RIGHT
                    and current_image_path
                    is not None
                ):

                    total_skipped += 1
                    total_processed += 1

                    image_index += 1

                    save_progress(
                        folder_index,
                        image_index,
                        total_processed,
                        total_good,
                        total_skipped,
                        folders_completed,
                    )

                    current_image_path = None

                    current_image_surface = None

                # --------------------------------------------
                # LEFT = PREVIOUS
                # --------------------------------------------

                elif event.key == pygame.K_LEFT:

                    if image_index > 0:

                        image_index -= 1

                        current_image_path = None

                        current_image_surface = None

                        save_progress(
                            folder_index,
                            image_index,
                            total_processed,
                            total_good,
                            total_skipped,
                            folders_completed,
                        )

        # ----------------------------------------------------
        # DRAW
        # ----------------------------------------------------

        screen.fill(BLACK)

        if all_done:

            draw_text(
                screen,
                "ALL FOLDERS COMPLETED",
                font_large,
                GREEN,
                screen.get_width() // 2,
                screen.get_height() // 2 - 30,
                center=True,
            )

            draw_text(
                screen,
                f"Good: {total_good}",
                font_medium,
                GREEN,
                screen.get_width() // 2,
                screen.get_height() // 2 + 20,
                center=True,
            )

            draw_text(
                screen,
                "Press ESC to exit",
                font_small,
                GRAY,
                screen.get_width() // 2,
                screen.get_height() // 2 + 60,
                center=True,
            )

        elif current_image_surface:

            draw_centered_image(
                screen,
                current_image_surface,
            )

        # ----------------------------------------------------
        # Bottom status bar
        # ----------------------------------------------------

        status_height = 100

        pygame.draw.rect(
            screen,
            DARK_GRAY,
            (
                0,
                screen.get_height()
                - status_height,
                screen.get_width(),
                status_height,
            ),
        )

        # ----------------------------------------------------
        # LEFT SIDE: folder / progress
        # ----------------------------------------------------

        if not all_done:

            folder = folders[
                folder_index
            ]

            draw_text(
                screen,
                folder.name,
                font_medium,
                WHITE,
                15,
                screen.get_height() - 92,
            )
            draw_text(
                screen,
                current_image_path.name if current_image_path else "",
                font_small,
                WHITE,
                15,
                screen.get_height() - 42,
            )
            photo_number = min(
                image_index + 1,
                len(current_images),
            )

            draw_text(
                screen,
                f"Photo {photo_number} / {len(current_images)}",
                font_small,
                GRAY,
                15,
                screen.get_height() - 62,
            )

        # ----------------------------------------------------
        # LEFT SIDE: counters
        # ----------------------------------------------------

        draw_text(
            screen,
            f"Good: {total_good}",
            font_small,
            GREEN,
            230,
            screen.get_height() - 75,
        )

        draw_text(
            screen,
            f"Skipped: {total_skipped}",
            font_small,
            YELLOW,
            330,
            screen.get_height() - 75,
        )

        draw_text(
            screen,
            # f"Processed: {total_processed}",
            f"Processed: {total_processed} / {total_files}",
            font_small,
            WHITE,
            455,
            screen.get_height() - 75,
        )

        # ----------------------------------------------------
        # CONTROLS
        # ----------------------------------------------------

        draw_text(
            screen,
            "G = GOOD    SPACE = NEXT    ← = PREVIOUS    ESC = EXIT",
            font_small,
            GRAY,
            700,
            screen.get_height() - 75,
        )

        pygame.display.flip()

        clock.tick(60)

    pygame.quit()


# ============================================================
# ENTRY POINT
# ============================================================

if __name__ == "__main__":
    main()