import os
import re
import sqlite3

# --- CONFIGURATION ---
TARGET_FOLDER = r"/Users/sriram/Desktop/yard/temples/"  # Path containing the subfolders
DB_PATH = "/Users/sriram/Desktop/yard/rest/bin/database/temples.db"  # Path to your SQLite database file
TABLE_NAME = "temples"  # Name of the DB table
COLUMN_NAME = "dm"  # Name of the column containing shortnames


def get_missing_shortnames():
    # 1. Connect to the SQLite Database
    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()

        # Fetch all existing shortnames into a set for O(1) ultra-fast lookups
        cursor.execute(f"SELECT {COLUMN_NAME} FROM {TABLE_NAME}")
        # Using a set handles case-insensitivity comparison cleanly if lowercased
        db_shortnames = {row[0].strip().lower() for row in cursor.fetchall() if row[0]}

    except sqlite3.Error as e:
        print(f"Database error: {e}")
        return
    finally:
        if "conn" in locals():
            conn.close()

    # 2. Scan the directory for subfolders
    if not os.path.exists(TARGET_FOLDER):
        print(f"Error: The folder path '{TARGET_FOLDER}' does not exist.")
        return

    print("Scanning subfolders and verifying against database...\n")
    missing_count = 0

    # Iterates only through immediate subdirectories
    for entry in os.scandir(TARGET_FOLDER):
        if entry.is_dir():
            folder_name = entry.name

            # Regex pattern to match 'anything-shortname'
            # This captures everything after the FIRST hyphen
            match = re.match(r"^.*-(.+)$", folder_name)

            if match:
                shortname = match.group(1).strip()
                shortname_lower = shortname.lower()

                # 3. Check if the extracted shortname exists in the DB set
                if shortname_lower not in db_shortnames:
                    print(f"Missing: '{shortname}' (From folder: {folder_name})")
                    missing_count += 1
            else:
                print(
                    f"Skipped: '{folder_name}' (Does not match 'townname-shortname' format)"
                )

    if missing_count == 0:
        print("\nAll subfolder shortnames exist in the database.")
    else:
        print(f"\nTotal missing shortnames found: {missing_count}")


if __name__ == "__main__":
    get_missing_shortnames()