import os
import shutil
import time
import pyautogui
import subprocess
import sys
import pygetwindow as gw

source_dir = '' # Put your Neoclient Path here.
destination_dir = '' # Put your mods folder here.



# Delete existing .jar files in the mods folder -- Only if you arent using any other mods
for file_name in os.listdir(destination_dir):
    if file_name.endswith('.jar'):
        full_file_name = os.path.join(destination_dir, file_name)
        if os.path.isfile(full_file_name):
            os.remove(full_file_name)

# Move new .jar files to the mods folder
for file_name in os.listdir(source_dir):
    if file_name.endswith('.jar'):
        full_file_name = os.path.join(source_dir, file_name)
        if os.path.isfile(full_file_name):
            shutil.move(full_file_name, destination_dir)


# You can remove this section if you arent using Legacy Launcher

time.sleep(0.3)

subprocess.run(["powershell", "-Command", "Add-Type -AssemblyName Microsoft.VisualBasic; [Microsoft.VisualBasic.Interaction]::AppActivate('Legacy Launcher 159.10 [Stable]')"]) # Change to Window Name


pyautogui.hotkey('win', 'up')

screen_width, screen_height = pyautogui.size()

x_center = screen_width // 2
y_30_percent_from_bottom = screen_height - int(screen_height * 0.43)

pyautogui.moveTo(x_center, y_30_percent_from_bottom)
pyautogui.click()

# End of the section

sys.exit()
