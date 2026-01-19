import os
import shutil
import filecmp

def sync_dirs(source, destination):
    # Ensure the destination directory exists
    if not os.path.exists(destination):
        os.makedirs(destination)

    # Walk through the source directory
    for root, dirs, files in os.walk(source):
        # Compute the relative path to the destination
        relative_path = os.path.relpath(root, source)
        dest_path = os.path.join(destination, relative_path)

        # Create directories in the destination if they don't exist
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)

        # Sync files
        for file in files:
            src_file = os.path.join(root, file)
            dest_file = os.path.join(dest_path, file)

            # Copy the file if it doesn't exist in the destination or is different
            if not os.path.exists(dest_file) or not filecmp.cmp(src_file, dest_file, shallow=False):
                shutil.copy2(src_file, dest_file)
                print(f"Copied: {src_file} -> {dest_file}")

        # Remove files in the destination that don't exist in the source
        for dest_file in os.listdir(dest_path):
            dest_file_full = os.path.join(dest_path, dest_file)
            src_file_full = os.path.join(root, dest_file)

            if not os.path.exists(src_file_full):
                os.remove(dest_file_full)
                print(f"Deleted: {dest_file_full}")

    # Remove empty directories in the destination that don't exist in the source
    for root, dirs, files in os.walk(destination, topdown=False):
        relative_path = os.path.relpath(root, destination)
        src_path = os.path.join(source, relative_path)

        if not os.path.exists(src_path):
            os.rmdir(root)
            print(f"Removed directory: {root}")

# Define source and destination directories
source_dir = "/Volumes/512-gb-yuva/pics"
destination_dir = "/Volumes/yuva-rb64gb/pics"

# Sync the directories
sync_dirs(source_dir, destination_dir)
print("Sync complete!")