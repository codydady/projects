import os
import shutil

def delete_hidden_files(directory):
    # List of protected files/directories to skip
    protected = {'.Spotlight-V100', '.Trashes', '.fseventsd', '.DS_Store'}

    # Walk through the directory and its subdirectories
    for root, dirs, files in os.walk(directory):
        for file in files:
            # Check if the file is hidden (starts with a dot) and not protected
            if file.startswith('.') and file not in protected:
                file_path = os.path.join(root, file)
                try:
                    print(f"Deleting hidden file: {file_path}")
                    os.remove(file_path)  # Delete the hidden file
                except PermissionError:
                    print(f"Skipping protected file: {file_path} (Permission Denied)")

        # Optionally, delete hidden directories as well
        for dir in dirs:
            if dir.startswith('.') and dir not in protected:
                dir_path = os.path.join(root, dir)
                try:
                    print(f"Deleting hidden directory: {dir_path}")
                    shutil.rmtree(dir_path)  # Delete the hidden directory and its contents
                except PermissionError:
                    print(f"Skipping protected directory: {dir_path} (Permission Denied)")

# Specify the directory to clean
directory_to_clean = "/Volumes/512-gb-yuva"

# Call the function to delete hidden files
delete_hidden_files(directory_to_clean)
print("Hidden files and directories deleted.")
