#!/usr/bin/env python3
"""
Script to update temple visit dates from recently modified folders
Combines original logic with date threshold checking
"""

import sqlite3
import os
import time
from PIL import Image
from PIL.ExifTags import TAGS
from datetime import datetime

# Configuration
DB_PATH = "/Users/sriram/Desktop/yard/rest/bin/database/temples.db"
ROOT_DIR = "/Users/sriram/Desktop/yard/temples/"
DAYS_THRESHOLD = 7  # Process folders modified in last 7 days

def get_date_taken(image_path):
    """Extract date from image EXIF data (original logic)"""
    try:
        with Image.open(image_path) as img:
            exif_data = img._getexif()
            if exif_data is not None:
                for tag_id, value in exif_data.items():
                    tag_name = TAGS.get(tag_id, tag_id)
                    if tag_name == 'DateTimeOriginal':
                        datetime_obj = datetime.strptime(value, '%Y:%m:%d %H:%M:%S')
                        return datetime_obj.strftime('%Y %m')
    except Exception as e:
        print(f"Error: {e}")
    return None

def extract_info_from_folder(folder_path):
    """Get all unique dates from images in folder (original logic)"""
    dates = []
    files = [file for file in os.listdir(folder_path) 
             if os.path.isfile(os.path.join(folder_path, file))]
    files = [file for file in files if not file.startswith('.')]
    
    if not files:
        return dates

    for file in files:
        file_path = os.path.join(folder_path, file)
        date_taken = get_date_taken(file_path)
        if date_taken and date_taken not in dates:
            dates.append(date_taken)

    return sorted(dates) if dates else []

def is_folder_recent(folder_path):
    """Check if folder was modified/renamed recently"""
    cutoff = time.time() - (DAYS_THRESHOLD * 24 * 60 * 60)
    try:
        mtime = os.path.getmtime(folder_path)  # Last content modification
        ctime = os.path.getctime(folder_path)  # Last metadata change (including rename)
        return max(mtime, ctime) >= cutoff
    except OSError:
        return False

def get_temples_with_dm(db_path):
    """Get all temples with DM values (original logic)"""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("""
        SELECT temple_id, dm, name, place, visit_dt 
        FROM temples 
        WHERE dm IS NOT NULL
    """)
    records = cursor.fetchall()
    conn.close()
    return records

def find_folder_with_dm(root_folder, dm):
    """Find folder containing DM string (original logic with date check)"""
    for folder_path, dirs, _ in os.walk(root_folder):
        for folder in dirs:
            if dm in folder:
                full_path = os.path.join(folder_path, folder)
                if is_folder_recent(full_path):
                    return full_path
    return None

def update_visit_date(db_path, dm, dates):
    """Update visit_dt in database (original logic)"""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        if dates:
            formatted_dates = '; '.join(
                datetime.strptime(d, '%Y %m').strftime('%b %Y').lower() 
                for d in dates
            )
            cursor.execute(
                "UPDATE temples SET visit_dt = ? WHERE dm = ?",
                (formatted_dates, dm)
            )
        else:
            cursor.execute(
                "UPDATE temples SET visit_dt = '-' WHERE dm = ?",
                (dm,)
            )
        conn.commit()
        return True
    except sqlite3.Error as e:
        print(f"Database error updating {dm}: {e}")
        return False
    finally:
        conn.close()

def main():
    print(f"Updating visit dates from folders modified in last {DAYS_THRESHOLD} days...")
    
    temples = get_temples_with_dm(DB_PATH)
    print(f"Found {len(temples)} temples with DM values")
    
    updated_count = 0
    for temple in temples:
        temple_id, dm, name, place, visit_dt = temple
        # print(f"\nProcessing: {name} ({place}) | DM: {dm}")
        
        folder_path = find_folder_with_dm(ROOT_DIR, dm)
        if not folder_path:
            # print("  No recent folder found for this DM")
            continue
            
        print(f"  Found recent folder: {os.path.basename(folder_path)} | DM: {dm}")
        dates = extract_info_from_folder(folder_path)
        
        if update_visit_date(DB_PATH, dm, dates):
            if dates:
                print(f"  Updated with dates: {', '.join(dates)}")
            else:
                print("  Set to default ('-')")
            updated_count += 1
    
    print(f"\nComplete! Updated {updated_count} temples")

if __name__ == "__main__":
    main()