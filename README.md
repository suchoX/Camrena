# Camrena
Camrena is a an Android app that lets you take Photos/Videos and Auto-Sync them to cloud. 

Captured Photos are stored in: internal/Camrena/Photos

Captured Videos are stored in: internal/Camrena/Videos

### Features
1. Photo and Video capture in single activity.
2. The photos and videos are automatically uploaded to cloud(Kinvey), via background service, which can be turned off.
3. Photos and videos deleted from local memory but synced can be viewed (via Picasso)
4. Realm local database
5. Recyclerview as Gridview for Image/Video gallery
6. Auto resize of the Camera preview to a supported camera resolution to prevent preview stretch. ie. if the device has a 16:9 display resolution, but camera image supports only 4:3 resolutions, the Camera preview will resize to a 4:3 preview

### Auto-Sync Feature (Check the icons on the Image/Video top)

1. Image/Video is present in local memory but not synced (Accessed Locally)-

<img src="https://github.com/suchoX/Camrena/blob/master/Screens/local_notsynced.png" alt="Drawing" style="width: 144px;"/>

2. Image/Video is present in local memory and synced to cloud (Accessed Locally)-

![alt text](https://github.com/suchoX/Camrena/blob/master/Screens/local_synced.png)

3. Image/Video is not present in memory, but synced to cloud (Accessed from Cloud)-

![alt text](https://github.com/suchoX/Camrena/blob/master/Screens/cloud_synced.png)

4. Image/Video is not present in memory and also not synced to cloud (Error as file not available)-

![alt text](https://github.com/suchoX/Camrena/blob/master/Screens/error.png)

