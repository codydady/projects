#!/usr/bin/env python3
"""
Script to update Tamil translations for temple names and places in SQLite database.
Handles cases where tam_name or tam_place are NULL or empty strings.
"""

import os
import sqlite3
import requests
import time
from functools import lru_cache
import logging

# ---- Configuration ----
CACHE_DB = "transliteration_cache.db"
BATCH_SIZE = 100
API_DELAY = 1  # seconds between API calls
MAX_API_TIMEOUT = 10  # seconds

# ---- Logging Setup ----
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# ---- Cache Setup ----
def init_cache_db(conn):
    """Initialize SQLite cache database."""
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS cache (
            english_text TEXT PRIMARY KEY,
            tamil_text TEXT,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()

@lru_cache(maxsize=1000)
def get_cached_transliteration(english_text):
    """Check cache for existing translations."""
    conn = sqlite3.connect(CACHE_DB)
    cursor = conn.cursor()
    cursor.execute("SELECT tamil_text FROM cache WHERE english_text = ?", (english_text,))
    result = cursor.fetchone()
    conn.close()
    return result[0] if result else None

def save_to_cache(conn, english_text, tamil_text):
    """Save new translations to cache."""
    cursor = conn.cursor()
    cursor.execute("""
        INSERT OR REPLACE INTO cache (english_text, tamil_text)
        VALUES (?, ?)
    """, (english_text, tamil_text))
    conn.commit()

# ---- Translation Service ----
def google_transliterate(text, max_retries=5):
    """Get Tamil translation for English text with retries."""
    if not text or not str(text).strip():
        return ""
    
    cached = get_cached_transliteration(text)
    if cached:
        return cached

    url = f"https://inputtools.google.com/request?text={text}&itc=ta-t-i0-und"
    for attempt in range(max_retries):
        try:
            response = requests.get(url, timeout=MAX_API_TIMEOUT)
            if response.status_code == 200:
                tamil_text = response.json()[1][0][1][0]
                return tamil_text
        except Exception as e:
            logging.warning(f"Attempt {attempt+1}/{max_retries} failed for '{text}': {str(e)}")
            time.sleep(2 ** attempt)
    
    logging.error(f"Failed after {max_retries} attempts for: {text}")
    return text  # Fallback to original

# ---- Database Operations ----
def get_temples_needing_update(conn, limit, offset):
    """Fetch batch of temples needing Tamil translations."""
    cursor = conn.cursor()
    cursor.execute("""
        SELECT dm, name, place, tam_name, tam_place 
        FROM temples 
        WHERE dm IS NOT NULL
        AND (tam_name IS NULL OR TRIM(tam_name) = '' 
             OR tam_place IS NULL OR TRIM(tam_place) = '')
        LIMIT ? OFFSET ?
    """, (limit, offset))
    return cursor.fetchall()

def update_temple_translations(conn, updates):
    """Update database with new translations."""
    cursor = conn.cursor()
    for dm, tam_name, tam_place in updates:
        try:
            cursor.execute("""
                UPDATE temples 
                SET tam_name = COALESCE(?, tam_name),
                    tam_place = COALESCE(?, tam_place)
                WHERE dm = ?
            """, (tam_name, tam_place, dm))
        except sqlite3.Error as e:
            logging.error(f"Database error updating {dm}: {str(e)}")
    conn.commit()

# ---- Main Processing ----
def process_temples(db_path):
    """Main function to update Tamil translations."""
    # Single connection for cache and main DB
    cache_conn = sqlite3.connect(CACHE_DB)
    db_conn = sqlite3.connect(db_path)
    
    init_cache_db(cache_conn)
    processed = 0
    
    # Get total count
    cursor = db_conn.cursor()
    cursor.execute("""
        SELECT COUNT(*) FROM temples 
        WHERE dm IS NOT NULL
        AND (tam_name IS NULL OR TRIM(tam_name) = '' 
             OR tam_place IS NULL OR TRIM(tam_place) = '')
    """)
    total = cursor.fetchone()[0]
    logging.info(f"Found {total} temples needing Tamil translations")
    
    while processed < total:
        batch = get_temples_needing_update(db_conn, BATCH_SIZE, processed)
        if not batch:
            logging.info(f"Stopping: No more temples to process at {processed}/{total}")
            break
        
        updates = []
        for dm, name, place, curr_tam_name, curr_tam_place in batch:
            try:
                new_tam_name = None
                new_tam_place = None
                
                if not curr_tam_name or not str(curr_tam_name).strip():
                    new_tam_name = google_transliterate(name)
                    if new_tam_name != name:  # Only cache if translated
                        save_to_cache(cache_conn, name, new_tam_name)
                
                if place and (not curr_tam_place or not str(curr_tam_place).strip()):
                    new_tam_place = google_transliterate(place)
                    if new_tam_place != place:
                        save_to_cache(cache_conn, place, new_tam_place)
                
                if new_tam_name is not None or new_tam_place is not None:
                    updates.append((dm, new_tam_name, new_tam_place))
                    logging.info(f"Processed {dm}: name='{name}' → '{new_tam_name}', place='{place}' → '{new_tam_place}'")
            
            except Exception as e:
                logging.error(f"Error processing {dm}: {str(e)}")
                continue
        
        if updates:
            update_temple_translations(db_conn, updates)
        
        processed += len(batch)
        logging.info(f"Completed batch: {processed}/{total}")
        time.sleep(API_DELAY)
    
    cache_conn.close()
    db_conn.close()

def main():
    db_path = "/Users/sriram/Desktop/yard/rest/bin/database/temples.db"
    process_temples(db_path)

if __name__ == "__main__":
    main()