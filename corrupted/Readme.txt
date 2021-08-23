A manual built (not by deployBundle.sh) corrupted bundle for tests:

- ControlLight.bin is listed in directory, but doesn't exist. Returns an error when delayed.
- missing.ac returns an error immediately. No gltf file exists for it.
