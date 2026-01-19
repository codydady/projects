#!/usr/bin/env python3
"""
Script to update SQLite database with temple info from recently modified/renamed folders.
Handles macOS folder rename cases by checking ctime (status change time).
"""

import os
import sqlite3
import time
from datetime import datetime

def get_folder_status(folder_path):
    """Get both mtime and ctime for a folder."""
    return {
        'mtime': os.path.getmtime(folder_path),  # Content modification time
        'ctime': os.path.getctime(folder_path)   # Metadata change time (includes renames)
    }

def process_recent_folders(root_folder, days_threshold=7):
    """
    Find all folders modified or renamed within the time threshold.
    Returns: List of tuples (folder_path, folder_name, change_type, change_time)
    """
    cutoff_time = time.time() - (days_threshold * 24 * 60 * 60)
    recent_folders = []

    for dirpath, dirnames, _ in os.walk(root_folder):
        for dirname in dirnames:
            full_path = os.path.join(dirpath, dirname)
            times = get_folder_status(full_path)
            
            # Determine if folder was recently changed or renamed
            if times['mtime'] >= cutoff_time and times['ctime'] >= cutoff_time:
                change_type = "content_modified"
                change_time = times['mtime']
            elif times['ctime'] >= cutoff_time:
                change_type = "renamed"
                change_time = times['ctime']
            else:
                continue
            
            recent_folders.append((full_path, dirname, change_type, change_time))
    
    return recent_folders

def update_database(db_path, folder_data):
    """Update SQLite database with folder information."""
    conn = None
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        for full_path, dirname, change_type, change_time in folder_data:
            if '-' not in dirname:
                continue
                
            a, b = dirname.split('-', 1)
            timestamp = datetime.fromtimestamp(change_time).strftime('%Y-%m-%d %H:%M:%S')
            
            try:
                cursor.execute(
                    "UPDATE temples SET place = ? WHERE dm = ?",
                    (a, b)
                )
                conn.commit()
                print(f"✅ Updated: {dirname} (Change: {change_type} at {timestamp})")
            except sqlite3.Error as e:
                print(f"❌ Database error for {dirname}: {e}")
                
    except Exception as e:
        print(f"⚠️ Critical error: {e}")
    finally:
        if conn:
            conn.close()

def main():
    # Configuration
    root_folder = "/Users/sriram/Desktop/yard/temples/"
    db_path = "/Users/sriram/Desktop/yard/rest/bin/database/temples.db"
    days_threshold = 30  # Process folders changed in last 7 days
    
    print(f"🔍 Scanning for recently changed folders in: {root_folder}")
    
    # Find recently modified/renamed folders
    recent_folders = process_recent_folders(root_folder, days_threshold)
    
    if not recent_folders:
        print("ℹ️ No recently changed folders found")
        return
    
    print(f"📁 Found {len(recent_folders)} recently changed folders:")
    for _, dirname, change_type, change_time in recent_folders:
        timestamp = datetime.fromtimestamp(change_time).strftime('%Y-%m-%d %H:%M:%S')
        print(f" - {dirname} ({change_type} at {timestamp})")
    
    # Update database
    print("\n💾 Updating database...")
    update_database(db_path, recent_folders)
    print("✅ Database update complete")

if __name__ == "__main__":
    main()