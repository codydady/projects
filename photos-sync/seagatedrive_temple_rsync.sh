#!/bin/bash

# Configuration
SOURCE_DIR="/Users/sriram/Desktop/yard/temples"
#TARGET_DIR="/Volumes/1-tb-yuva/temples"
TARGET_DIR="/Volumes/1-tb-seagate/temples"
LOG_FILE="/tmp/temple_sync.log"

# Check if target is mounted
if [ ! -d "$TARGET_DIR" ]; then
  echo "ERROR: Target directory not mounted at $TARGET_DIR" | tee -a "$LOG_FILE"
  exit 1
fi

# Sync with verbose output and logging
echo "=== Starting sync: $(date) ===" | tee -a "$LOG_FILE"
rsync -av --delete --progress "$SOURCE_DIR/" "$TARGET_DIR/" 2>&1 | tee -a "$LOG_FILE"

# Report completion
echo "=== Sync completed: $(date) ===" | tee -a "$LOG_FILE"
echo "Changes synced from $SOURCE_DIR to $TARGET_DIR" | tee -a "$LOG_FILE"
