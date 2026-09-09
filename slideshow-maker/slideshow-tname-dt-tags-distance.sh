# Ask the user a question
read -p "did you use the image dating program for all new temples? (yes/no): " answer

# Convert the answer to lowercase
answer=$(echo "$answer" | tr '[:upper:]' '[:lower:]')

# Check if the answer is "yes"
if [ "$answer" == "yes" ]; then
    echo "proceeding with the next step..."
    # Continue with the rest of your program here
else
    echo "operation cancelled."
    exit 1  # Exit the program with a non-zero status
fi

echo "temple slideshow program with random pics using details from sqlite database start... `date`" 

# this query makes the temples-dm.csv file , just noting the sql here for record
# SELECT dm, name, place, latlong , tags  FROM newschema.temple where complete = 'y' and dm is not null;

#set a different temple prefix so my doubts about same file being overlayed are overcome
tnprefx=`openssl rand -base64 15 | tr -dc 'a-zA-Z0-9' | head -c4`
# Record start time
start_time=$(date +%s)

# if on local mac hd
photo_folder='/Users/sriram/Desktop/yard/temples/'

# Set your database file path
DB_FILE="/Users/sriram/Desktop/yard/rest/bin/database/temples.db"	#------- change this when this changes --------

#if on external drive
# photo_folder='/Volumes/yuvas-ext-hdd/temples/chaya_tem'

junk_folder=~/junk/slideshow-junk
slideshow_folder=~/junk/slideshow-1

rm -rf $junk_folder
mkdir -p $junk_folder

rm -rf $slideshow_folder
mkdir -p $slideshow_folder

let i=0
let var=0

# for alphabetic file names , use this for upto 3999 files , else use more 
alpha_nmary=( $(seq -w 1 4000) )

#first slide
# cp $first_file $slideshow_folder/temple_${alpha_nmary[$i]}.jpg

let i=i+1

# List of color strings
color_list=(
    "snow"
    "GhostWhite"
    "AliceBlue"
    "Azure"
    "MintCream"
    "Honeydew"
    "FloralWhite"
    "Ivory"
    "Seashell"
    "LightCyan1"
    "LightYellow1"
    "PaleTurquoise1"
    "PaleGreen1"
    "LightSteelBlue1"
    "khaki1"
    "yellow1"
    "aquamarine1"
)

image_file_list=`find $photo_folder -type d | sort -R`

temp_file_odd_even=true

for temple_img_file in $image_file_list
do 
	currentdir=`basename $temple_img_file`
	place_value_from_folder=$(echo "$currentdir" | cut -d '-' -f 1)

	# this is to omit the parent god directory which also shows up
	if [[ "$place_value_from_folder" =~ ^(murugan|others|shakthi|shivan|vishnu|temples)$ ]]; 
	then 
		echo "value of currentdir is $place_value_from_folder , so skipping..."  
		continue
	fi
	
	# get a random temple from the current folder
	currentfile=$temple_img_file/`ls $temple_img_file |sort -R |tail -1`

	# 1. convert the image to 1000x750 size or location.png if the file is a directory 
	magick ${currentfile} -resize 1000x750 -quality 100 $junk_folder/${tnprefx}_${alpha_nmary[$i]}.jpg 

	dm_value_from_folder=$(echo "$currentdir" | cut -d '-' -f 2)
	#echo "value of currentdir is $currentdir , dm_value_from_folder is $dm_value_from_folder"  

	# Execute the SQL query
	# RESULT=$(sqlite3 "$DB_FILE" "SELECT name, visit_dt, latlong, tags, nearby_town, distance FROM temples WHERE dm = '$dm_value_from_folder';")
	RESULT=$(sqlite3 "$DB_FILE" -cmd ".mode list" -cmd ".separator |" "SELECT name, visit_dt, latlong, tags, nearby_town, distance FROM temples WHERE dm = '$dm_value_from_folder';")

	# Check if any rows were returned
	if [ -z "$RESULT" ]; then
		echo "--------------------------- <$dm_value_from_folder> is not in the database !!! ------------------------- "
	else
	    item=$RESULT
	    # result looks like this 
	    # echo "item is $item"
	    # sakidhaeviyammai sametha sathyagireeswarar|sambandhar,thevaram,thiruvidaimarudhoor-parivara-sthalam|apr 2007; mar 2016; feb 2020
	  	temple_name=$(echo "$item" | cut -d '|' -f 1 | sed 's/"//g' | tr '[:upper:]' '[:lower:]')

  		vdate=$(echo "$item" | cut -d '|' -f 2)
		vdate=${vdate//\"/}

  		latlong=$(echo "$item" | cut -d '|' -f 3 | sed 's/ //g')
  		# echo "latlong is $latlong"

  		tagcount=$(echo "$item" | cut -d '|' -f 4 | tr -cd ',' | wc -c)
  		# temple_tags=$(echo "$item" | cut -d '|' -f 4 | sed 's/,/ #/g' ) 
		temple_tags=$(echo "$item" | cut -d '|' -f 4 | sed -E 's/(.+)/#\1/; s/,/ #/g')
		# echo "temple tags = $temple_tags , item - $item, tagcount = $tagcount"

		nearbytown=$(echo "$item" | cut -d '|' -f 5)

		distfromnearbytown=$(echo "$item" | cut -d '|' -f 6)
	fi

	# else get the file for proessing
	place_value_from_folder=$(echo "$currentdir" | cut -d '-' -f 1)

	# remove trailing spaces
	temple_name=`echo $temple_name | sed 's/ *$//g'`

	# do the following for printing purposes
	cf=$(echo "$currentfile" | awk -F "temples" '{print $2}')
	
	# Calculate the index of a random value
	index=$(( RANDOM % ${#color_list[@]} ))
	chosen_color="${color_list[index]}"

	# for english text ,  change -font below for tamizh font - todo

	echo "cf = $cf , chosen_temble = $temple_name"

	# 1. Build a single multi-line string (Bash handles newline formatting)
	text_content="${distfromnearbytown} kms from ${nearbytown}"
	if [ -n "$temple_tags" ]; then
		text_content="${text_content}"$'\n'"${temple_tags}"
	fi

			# -font Trebuchet-MS -pointsize 18 \
			# -font Palatino -pointsize 18 \
			# -font Georgia -pointsize 18 \
			# -font Baskerville -pointsize 18 \

	# 2. Single magick command for both 2-line and 3-line cases
	# magick $junk_folder/${tnprefx}_${alpha_nmary[$i]}.jpg \
	# 	\( \
	# 		-font Trebuchet-MS -pointsize 18 \
	# 		-background "${chosen_color}" -fill black \
	# 		label:"$text_content" \
	# 		-bordercolor "${chosen_color}" -border 8x5 \
	# 	\) \
	# 	-geometry +100+540 -composite \
	# 	$slideshow_folder/${tnprefx}_${alpha_nmary[$i]}.jpg
		
	magick "$junk_folder/${tnprefx}_${alpha_nmary[$i]}.jpg" \
    \( \
        -font Trebuchet-MS -pointsize 18 \
        -background "${chosen_color}" -fill black \
        label:"${temple_name}, ${place_value_from_folder}" \
        -bordercolor "${chosen_color}" -border 8x5 \
    \) \
    -geometry +100+540 -composite \
    \( \
        -font Trebuchet-MS -pointsize 16 \
        -background "${chosen_color}" -fill "#445544" \
        label:"${text_content}" \
        -bordercolor "${chosen_color}" -border 8x5 \
    \) \
    -geometry +100+568 -composite \
    "$slideshow_folder/${tnprefx}_${alpha_nmary[$i]}.jpg"
	let i=i+1

	# stopper for test purposes , normally 10, but since i want for 40 mins, i gave 630 x 4 secs / pic
	let var=var+1
	if [ "$var" -ge 630 ]; then
		break
	fi

done

#copy end slide
# cp $last_file $slideshow_folder/f${alpha_nmary[$i]}.jpg

# Record end time
end_time=$(date +%s)

# Calculate the running time
running_time=$(( end_time - start_time ))

echo "total temples processed $i , running time: $running_time seconds"

echo 'temple slideshow program end...'
