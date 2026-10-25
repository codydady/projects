echo "Resizing source JPG/JPEG files to 640x480..."

for file in *.[jJ][pP][gG] *.[jJ][pP][eE][gG]; do
    [ -f "$file" ] || continue

    # Skip files already renamed by this script
    case "$file" in
        simg*.jpg) continue ;;
    esac

    magick "$file" \
        -auto-orient \
        -resize '640x480>' \
        -background white \
        -gravity center \
        -extent 640x480 \
        "$file.tmp.jpg" &&
    mv "$file.tmp.jpg" "$file"
done

echo "Renaming source files to simg*..."

i=1
for file in *.[jJ][pP][gG] *.[jJ][pP][eE][gG]; do
    [ -f "$file" ] || continue
    case "$file" in
        simg*.jpg) continue ;;
    esac

    mv -i "$file" "simg$i.jpg" && i=$((i+1))
done

echo "Renaming simg* files to img*.jpg for website use..."

i=1
for file in simg*.jpg; do
    [ -f "$file" ] || continue
    mv -i "$file" "img$i.jpg" && i=$((i+1))
done

echo "Done."