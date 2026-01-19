
import os
import random
from PIL import Image, ImageDraw, ImageFont

def process_images_with_overlay(input_main_folder, output_processed_folder):
    """
    Picks all photos from subfolders in a random order, overlays the folder name,
    resizes them, and saves them to a new folder.

    Args:
        input_main_folder (str): The path to the main folder containing subfolders of photos.
        output_processed_folder (str): The path where processed images will be saved.
    """

    # --- Configuration for Font ---
    # IMPORTANT: Replace this with the actual path to a .ttf or .otf font file on your system.
    # Examples:
    # Windows: "C:\\Windows\\Fonts\\arial.ttf" or "C:\\Windows\\Fonts\\segoeui.ttf"
    # macOS: "/System/Library/Fonts/Arial.ttf" or "/Library/Fonts/Arial.ttf"
    # Linux: "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    # You can also download a free font (e.g., from Google Fonts) and place it in the same directory as your script.
    font_path = '/Users/sriram/Desktop/loft/DejaVuSerif.ttf' # Set to None to use default fallback, or provide your path here
    # Example for Windows: font_path = "C:\\Windows\\Fonts\\arial.ttf"
    # Example for macOS: font_path = "/System/Library/Fonts/Arial.ttf"
    # Example for Linux: font_path = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


    # Create the output folder if it doesn't exist
    if not os.path.exists(output_processed_folder):
        os.makedirs(output_processed_folder)
        print(f"Created output folder: {output_processed_folder}")

    all_image_data = [] # Stores (full_image_path, subfolder_name)

    # Walk through the input folder to find all image files and their subfolder names
    print(f"Scanning images in: {input_main_folder}...")
    for root, dirs, files in os.walk(input_main_folder):
        # Extract the immediate subfolder name relative to the input_main_folder
        # This will be 'RootFolder' if images are directly in the input_main_folder,
        # otherwise it's the actual subfolder name.
        current_folder_name = os.path.basename(root)
        if current_folder_name == '': # If root is the input_main_folder itself and it's not named
            current_folder_name = os.path.basename(input_main_folder) # Use the main folder's name

        for filename in files:
            if filename.lower().endswith(('.png', '.jpg', '.jpeg')):
                full_image_path = os.path.join(root, filename)
                # Ensure we have a valid folder name to display
                display_folder_name = current_folder_name if current_folder_name else "Default Folder"
                all_image_data.append((full_image_path, display_folder_name))
    
    print(f"Found {len(all_image_data)} images. Shuffling order...")
    random.shuffle(all_image_data) # Randomize the order of processing

    processed_count = 0
    # Process each image
    for idx, (image_path, folder_name) in enumerate(all_image_data):
        try:
            print(f"Processing ({idx + 1}/{len(all_image_data)}): {image_path}")
            
            # Open and resize the image
            img = Image.open(image_path)
            img = img.resize((1000, 750), Image.LANCZOS) # Use LANCZOS for better quality resizing

            draw = ImageDraw.Draw(img)

            # --- Text Overlay Logic ---
            # Random light background color for the text
            bg_color = (random.randint(180, 255), random.randint(180, 255), random.randint(180, 255))
            text_color = (0, 0, 0) # Black text for readability

            # Attempt to load the specified font or fall back
            font_size = 26 # You can adjust this size
            try:
                if font_path and os.path.exists(font_path):
                    font = ImageFont.truetype(font_path, size=font_size)
                    # print(f"Using custom font: {font_path}")
                else:
                    # Fallback to common fonts if font_path is not set or invalid
                    try:
                        font = ImageFont.truetype("arial.ttf", size=font_size)
                    except IOError:
                        try:
                            font = ImageFont.truetype("DejaVuSans.ttf", size=font_size)
                        except IOError:
                            font = ImageFont.load_default() # Final fallback
                            print("Warning: Could not find Arial or DejaVuSans font and no custom font path set/valid. Using default bitmap font. Overlay might be small or pixelated.")
            except Exception as font_e:
                font = ImageFont.load_default()
                print(f"Error loading specified font ({font_path}). Using default bitmap font. Error: {font_e}")

# Calculate text bounding box to get its width and height accurately
            # (0,0) is just a reference point; we only care about width/height for sizing
            # The bbox returns (left, top, right, bottom)
            bbox = draw.textbbox((0, 0), folder_name, font=font)
            text_width = bbox[2] - bbox[0]
            text_height = bbox[3] - bbox[1]

            # Position for the overlay: bottom-left, with padding
            padding = 7 # Padding around the text inside the colored background
            x_start = padding
            x_start = 90

            y_start = img.height - text_height - padding
            y_start = 540

            # Background rectangle width: NOW ONLY TEXT_WIDTH + PADDING
            rect_width = text_width + (padding * 2) # Ensures padding on both sides of the text
            rect_height = text_height + (padding * 2) # Height includes padding above and below text

            # Draw the background rectangle
            # Ensure coordinates are within image bounds
            draw.rectangle(
                (x_start,
                 y_start,
                 min(x_start + rect_width, img.width), # Ensure box doesn't go off screen right
                 min(y_start + rect_height, img.height)), # Ensure box doesn't go off screen bottom
                fill=bg_color
            )

            # Draw the text on top of the background
            draw.text((x_start + padding, y_start + 3), folder_name, fill=text_color, font=font)
            # --- End Text Overlay Logic ---

            # Save the processed image
            output_filename = f"rlystn-{processed_count + 1}.jpg"
            output_filepath = os.path.join(output_processed_folder, output_filename)
            img.save(output_filepath, quality=90) # Save as JPEG with good quality

            processed_count += 1

        except Exception as e:
            print(f"Skipping {image_path} due to error: {e}")

    print(f"\nProcessing complete! {processed_count} images processed and saved to: {output_processed_folder}")

# --- Main execution block (remains the same) ---
if __name__ == "__main__":
    # Example Usage (replace with your actual folder paths)
    # input_main_folder = "/Users/sriram/Desktop/loft/media/test"  # Replace with the path to your main folder
    input_main_folder = "/Users/sriram/Desktop/yard/pics/railway-stations"  # Replace with the path to your main folder

    output_processed_folder = "/Users/sriram/Desktop/output_folder" # Replace with the desired output folder

    # Create dummy folders and a dummy image for testing if they don't exist
    # (You can remove this block once you're using real paths)
    if not os.path.exists(input_main_folder):
        os.makedirs(os.path.join(input_main_folder, "subfolder1"))
        dummy_image = Image.new("RGB", (800, 600), color="white")
        dummy_image.save(os.path.join(input_main_folder, "subfolder1", "dummy_image.jpg"))
        print(f"Created dummy input folder and image for testing in: {input_main_folder}")

    if not os.path.exists(output_processed_folder):
        os.makedirs(output_processed_folder)
        print(f"Created dummy output folder: {output_processed_folder}")
    # End of dummy folder creation block

    # Call the main processing function
    process_images_with_overlay(input_main_folder, output_processed_folder)
    print("Script finished.")
