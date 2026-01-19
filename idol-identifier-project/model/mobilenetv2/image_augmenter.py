import os
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator, load_img, img_to_array
import numpy as np

def augment_images(input_dir, output_dir, class_name, current_count, target_count):
    """
    Augment images for a minority class to reach target_count and save to output_dir.
    
    Args:
        input_dir (str): Path to directory containing class folder (e.g., 'dataset/hanuman').
        output_dir (str): Path to save augmented images (e.g., 'output/hanuman_augmented').
        class_name (str): Name of the class folder (e.g., 'hanuman').
        current_count (int): Current number of images (e.g., 200).
        target_count (int): Desired total images (e.g., 500).
    """
    # Calculate number of images to generate
    images_to_generate = target_count - current_count
    if images_to_generate <= 0:
        print(f"No augmentation needed: {class_name} already has {current_count} images.")
        return

    # Define input and output paths
    class_dir = os.path.join(input_dir, class_name)
    output_class_dir = os.path.join(output_dir, f"{class_name}_augmented")
    
    # Create output directory if it doesn't exist
    os.makedirs(output_class_dir, exist_ok=True)

    # Define augmentation parameters
    datagen = ImageDataGenerator(
        rotation_range=20,
        width_shift_range=0.2,
        height_shift_range=0.2,
        horizontal_flip=True,
        zoom_range=0.2,
        brightness_range=[0.8, 1.2],
        fill_mode='nearest'
    )

    # Get list of image files
    image_files = [f for f in os.listdir(class_dir) if f.endswith(('.jpg', '.jpeg', '.png'))]
    if not image_files:
        print(f"No images found in {class_dir}.")
        return

    # Generate augmented images
    generated_count = 0
    while generated_count < images_to_generate:
        # Randomly select an image
        img_path = os.path.join(class_dir, np.random.choice(image_files))
        
        # Load and preprocess image
        img = load_img(img_path, target_size=(224, 224))  # MobileNetV2 input size
        x = img_to_array(img)
        x = x.reshape((1,) + x.shape)  # Add batch dimension

        # Generate one augmented image per iteration
        for batch in datagen.flow(
            x,
            batch_size=1,
            save_to_dir=output_class_dir,
            save_prefix='aug',
            save_format='jpg'
        ):
            generated_count += 1
            print(f"Generated image {generated_count}/{images_to_generate}")
            break  # Exit after one image to control count

        if generated_count >= images_to_generate:
            break

    print(f"Augmentation complete: {generated_count} new images saved in {output_class_dir}")
    print(f"Total images for {class_name}: {current_count} original + {generated_count} augmented = {current_count + generated_count}")

def main():
    # Configuration
    input_dir = "../../data/"  # Directory containing 'hanuman' folder
    output_dir = "output"  # Directory for augmented images
    class_name = "temple"  # Minority class
    current_count = 203  # Current number of images
    target_count = 500  # Desired total images

    # Run augmentation
    augment_images(input_dir, output_dir, class_name, current_count, target_count)

if __name__ == "__main__":
    main()