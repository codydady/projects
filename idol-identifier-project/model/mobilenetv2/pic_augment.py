import os
import cv2
import albumentations as A
from glob import glob
from tqdm import tqdm

# Define the augmentation pipeline
transform = A.Compose([
    A.HorizontalFlip(p=0.5),
    A.RandomBrightnessContrast(p=0.2),
    A.RGBShift(r_shift_limit=15, g_shift_limit=15, b_shift_limit=15, p=0.3),
    A.GaussNoise(var_limit=(10.0, 50.0), p=0.2),
    A.ShiftScaleRotate(shift_limit=0.05, scale_limit=0.05, rotate_limit=15, p=0.5),
])

def augment_folder(folder_path, target_count):
    images = glob(os.path.join(folder_path, "*.jpg")) + glob(os.path.join(folder_path, "*.png"))
    current_count = len(images)
    
    if current_count >= target_count:
        print(f"Skipping {os.path.basename(folder_path)}, already has {current_count} images.")
        return

    needed = target_count - current_count
    print(f"Augmenting {os.path.basename(folder_path)}: Adding {needed} images...")

    pbar = tqdm(total=needed)
    counter = 0
    
    while counter < needed:
        for img_path in images:
            if counter >= needed: break
            
            image = cv2.imread(img_path)
            if image is None: continue
            
            # Apply transformation
            augmented = transform(image=image)['image']
            
            # Save with a prefix to avoid overwriting originals
            new_name = f"aug_{counter}_{os.path.basename(img_path)}"
            cv2.imwrite(os.path.join(folder_path, new_name), augmented)
            
            counter += 1
            pbar.update(1)
    pbar.close()

# Configuration
DATA_DIR = "./data"
folders_to_fix = {
    "hanuman": 1300,
    "murugan": 1300
}

if __name__ == "__main__":
    for folder, target in folders_to_fix.items():
        path = os.path.join(DATA_DIR, folder)
        if os.path.exists(path):
            augment_folder(path, target)
        else:
            print(f"Folder not found: {path}")