import os
import sqlite3

# Paths and settings
folder_path = "/Users/sriram/Desktop/yard/temples"
db_path = "/Users/sriram/Desktop/yard/rest/database/temples.db"  # Adjust if database is elsewhere
table_name = "temples"

# Connect to SQLite database
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# Get all dm values from the database
cursor.execute(f"SELECT dm FROM temples")
db_dms = {row[0] for row in cursor.fetchall()}  # Set for O(1) lookup

# Get all folder names and extract dm values
folder_dms = set()
for folder in os.listdir(folder_path):
    if os.path.isdir(os.path.join(folder_path, folder)):
        # Split by '-' and take the part after, assuming it's the dm
        parts = folder.split('-')
        if len(parts) > 1:
            dm = parts[-1]  # Last part after '-'
            folder_dms.add(dm)

# Find missing dm values
missing_dms = folder_dms - db_dms

# Output results
if missing_dms:
    print("Missing dm values (not in database):", missing_dms)
    print(f"Number of missing entries: {len(missing_dms)}")
else:
    print("No missing dm values found.")

# Close database connection
conn.close()