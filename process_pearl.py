import sys
from PIL import Image

def process_image(input_path, output_path):
    img = Image.open(input_path).convert("RGBA")
    data = img.getdata()
    
    # Get colors from the top left 100x100 corner to identify the checkerboard
    bg_colors = set()
    width, height = img.size
    for y in range(100):
        for x in range(100):
            bg_colors.add(img.getpixel((x, y)))
            
    print(f"Background colors identified: {bg_colors}")
    
    # Replace background colors with transparent
    new_data = []
    for item in data:
        if item in bg_colors:
            new_data.append((0, 0, 0, 0))
        else:
            new_data.append(item)
            
    img.putdata(new_data)
    
    # Crop to bounding box
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        
    # Resize to 32x32
    img = img.resize((32, 32), Image.Resampling.LANCZOS)
    
    # Save
    img.save(output_path)
    print(f"Saved to {output_path}")

if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    process_image(input_file, output_file)
