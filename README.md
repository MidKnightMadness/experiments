# Assemblies
This experiment copied the TeleOp behavior of the 2016-2017 Main Bot and MiniBot. It uses a new format where each subassembly is split into seperate directories and each subassembly is divided into a controller and TeleOp. Subassemblies are combined to make each robot. When a subassembly needs informaition about the robot configuration or other assemblies, it goes to a common CrossCommunicator. This allows each subassembly to not reference other subassembies so each subassembly can be developed independently.

Simple autonomous examples are also included, but they do not use this format.

## How to Get It All Working on Your Computer

### 1. Get git

### 2. Get ftc_app Working
- Download Java SDK
- Download Android Studio
- Clone ftc_app repository

### 3. Get assemblies Working
- Clone this repository
- Copy MainBot/ and MiniBot/ directories into ftc_app/TeamCode/src/main/java/org/firstinspires/ftc/teamcode
- Build and Install on Robot Controller
- Each assembly has its own TeleOp, as well as Main Bot and Mini Bot (good luck running it w/o the bots though)

- run ssh-keygen to generate id_rsa in .ssh if on mac