# Camrena
Camrena is a an Android app that lets you take Photos/Videos and Auto-Sync them to cloud. 

Captured Photos are stored in: internal/Camrena/Photos

Captured Videos are stored in: internal/Camrena/Videos

### Features
1. Photo and Video capture in single activity.
2. The photos and videos are automatically uploaded to cloud(Kinvey), via background service, which can be turned off.
3. Photos and videos deleted from local memory but synced can be viewed (used Picasso)
4. Photos Captured via front camera not mirrored. It has been handled internally.
5. Realm local database
6. Recyclerview as Gridview for Image/Video gallery
7. Auto resize of the Camera preview to a supported camera resolution to prevent preview stretch. ie. if the device has a 16:9 display resolution, but camera image supports only 4:3 resolutions, the Camera preview will resize to a 4:3 preview. Example (Asus Fonepad 7 K012 supports only 4:3 images in the front cam, but is a 16:9 screen device)-

![no_stretch](https://github.com/suchoX/Camrena/blob/master/Screens/no_stretch.jpg)

### Auto-Sync Feature (Check the icons on the Image/Video top)

1. Image/Video is present in local memory but not synced (Accessed Locally)-

![local_notsynced](https://github.com/suchoX/Camrena/blob/master/Screens/local_notsynced.png)

2. Image/Video is present in local memory and synced to cloud (Accessed Locally)-

![local_synced](https://github.com/suchoX/Camrena/blob/master/Screens/local_synced.png)

3. Image is not present in memory, but synced to cloud (Accessed from Cloud)-

![cloud_synced](https://github.com/suchoX/Camrena/blob/master/Screens/cloud_synced.png)

4. Image is not present in memory and also not synced to cloud (Error as file not available)-

![Error](https://github.com/suchoX/Camrena/blob/master/Screens/error.png)

### Issues
1. Videos are uploaded successfully via FileStream(Not getting reply when uploaded as File), and the uploaded video entry can be viewed on the app admin dashboard with the correct size and info. But Kinvey is not allowing us to view/stream/download the file, even if we use Kinvey's SDK to download. We get a "HTTP Error 416 Requested Range not satisfiable Explained" error, which is not documented

2. After an image has been clicked, to avoid freezing of UI, the photo storage is done in background, which takes about 1 sec. So sometimes, if we view gallery immediately after clicking a photo, it may not be visible in the gallery. Open gallery again, and the photo would be visible.

3. Front Camera Captured video is mirrored. Preview video while recording is not mirrored.

4. Won't work on 6.0+ devices as Camera permission has to be granted at runtime, which has not been implemented due to lack of a 6.0+ device.

