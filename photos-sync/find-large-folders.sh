#!/bin/bash

maxcount=40
echo "this program provides directories with over $maxcount files in it so unneeded pics can be deleted"

# Check if a directory path has been provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

# The directory to search
SEARCH_DIR=$1
total_count=0

# Loop through each directory recursively
while IFS= read -r -d '' dir; do
    # Count the number of files in the directory
    count=$(find "$dir" -maxdepth 1 -type f | wc -l)
    if [ "$count" -gt $maxcount ]; then
        # Print directories with more than 20 files
        echo "$count $dir"
        # Increment total count
        (( total_count++ ))
    fi
done < <(find "$SEARCH_DIR" -type d -print0)

echo "Total count of files in directories with more than $maxcount files: $total_count"
