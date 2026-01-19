echo "renaming files to simg* so even img1.jpg etc will be evenly transformed and not overwritten.."

i=1; for file in *.[jJ][pP][gG] *.[jJ][pP][eE][gG]; do [ -f "$file" ] && mv -i "$file" "simg$i.jpg" && i=$((i+1)); done

echo "renaming simg* files to  img*.jpg for use with website.."

 i=1; for file in *.jpg; do [ -f "$file" ] && mv -i "$file" "img$i.jpg" && i=$((i+1)); done
