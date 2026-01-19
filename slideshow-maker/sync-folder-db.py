import subprocess

# List of Python scripts to run
scripts = ["place_updater.py", "nearest_town_updater.py", "name_place_to_tamir_updater.py", "visited-dt-updater.py"]

# Function to run a Python script
def run_script(script_name):
    try:
        print(f"Running {script_name}...")
        subprocess.run(["python3", script_name], check=True)
        print(f"{script_name} completed successfully.")
    except subprocess.CalledProcessError as e:
        print(f"Error running {script_name}: {e}")
    except FileNotFoundError:
        print(f"Script {script_name} not found.")

# Run all scripts sequentially
for script in scripts:
    run_script(script)

print("All scripts executed.")