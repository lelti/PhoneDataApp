### **Intro**
_As someone with only Java knowledge but new to Android development, I focused on understanding Android components, permissions, and data extraction for this project._ 

_The app I created is simple but demonstrates fetching four key data points from the phone:_
1. **Device Model**
2. **Android Version**
3. **Installed apps** (with financial app detection)
4. **Device Location** (with user permission)

_Let me walk you through the flow and the thinking behind each part._

---

### **App Flow & onCreate()**
_The app starts in the `onCreate()` method — that’s basically the main entry point when the app screen shows up._

_Here, I set the layout using `setContentView()`, then I find references to the text views where I’ll display the information._  

_Next, I extract the device model and Android version using the Android `Build` class — very simple access to device info._  
```java
String deviceModel = Build.MODEL;
String androidVersion = Build.VERSION.RELEASE;
```
_Then, I fetch the installed apps using the `getLaunchableApps()` function, which I’ll explain in a bit. Finally, I initialize the location client to fetch the location._

_Everything is modular — so the extraction and displaying of device info, apps, and location happen in their own sections._  

---

### **Permissions Handling**
_Since location is sensitive data, I had to handle permissions. Android requires us to explicitly check if permissions are granted._  

_I do that in `checkAndRequestLocationPermission()`. If the permissions aren’t granted, it requests them. Otherwise, it proceeds to check the location settings._  

```java
ActivityCompat.requestPermissions(this,
   new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
   LOCATION_PERMISSION_REQUEST_CODE);
```
_Once the user responds, the `onRequestPermissionsResult()` handles their response — if allowed, we fetch the location; if not, I show a message._  

---

### **Fetching Installed Apps (with Finance App Check)**
_This part was interesting. I used the Android `PackageManager` to fetch all apps with a launch icon (basically user-installed apps that can be opened)._  

```java
Intent intent = new Intent(Intent.ACTION_MAIN, null);
intent.addCategory(Intent.CATEGORY_LAUNCHER);
List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
```

_For each app, I read the app name and package. Then, I check if it’s a financial app by scanning for keywords like 'bank', 'wallet', 'finance', or common names like 'Nordea' or 'Swish'._  

```java
if (isFinancialApp(appName, packageName)) {
   installedApps.append((Financial) );
}
```
_The result is a list of apps printed out in the app with finance-related ones marked. I imagine this would be helpful when extracting app usage data for AI models._  

---

### **Location Fetching Logic**
_For location, I used `FusedLocationProviderClient` because it’s more battery-friendly and smart — it uses GPS, Wi-Fi, and cell towers._  

_The flow is — check location settings, get last known location if available, or else start live updates._  

```java
fusedLocationClient.getLastLocation()
```
_If location is null, I request location updates every 10 seconds to get a fresh location:_
```java
locationRequest.setInterval(10000); // 10 seconds
locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
```
_When I get the location, I update the screen with the latitude and longitude._

_I also made sure to stop location updates in `onPause()` to avoid battery drain._  

---

### **Handling Location Settings and Failures**
_An interesting part was handling what happens if the user’s location is off. The app checks using the `LocationSettingsRequest` and if needed, prompts the user to turn it on:_
```java
ResolvableApiException resolvable = (ResolvableApiException) e;
resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
```
_If the user agrees, we fetch the location. If not, we notify the user that location is needed._

---

### **Lifecycle Awareness**
_I also added `onPause()` to stop location updates when the app isn’t visible. This is good practice for battery efficiency._  

```java
@Override
protected void onPause() {
    super.onPause();
    stopLocationUpdates();
}
```

---

### **Overall Learnings**
_For me, the key learning was understanding how Android apps interact with the device and the user — especially around permissions, which are very strict._  

✅ **Permissions Handling**  
✅ **Fetching real-time data**  
✅ **UI updates dynamically**  
✅ **Thinking about app lifecycle and resource management**

_I also realized that building for AI model data extraction means thinking about what data is useful, like finance apps or location, and being responsible about it._  

---

### **What I Would Improve / Next Steps**
_If I had more time or in the next iteration, I’d:_
- Store the data in a database or **send it to a backend API**
- Add tests
- Add **error handling** for cases like no network
- **Modularize** the code better into separate classes

---

### **Wrap Up**
_In summary, this project helped me understand Android basics — layouts, Java classes, permissions, working with system services like location and package manager._  

_While I’m junior and learning, this gave me confidence that I can handle tasks where AI models need structured phone data. I’m excited to improve this and take on bigger challenges in this area._  

_Happy to answer any questions or go into specific parts in more detail!_

---

### **Challenges Encountered**

1. **Setting Up Android Studio and Project Setup:**
   One of the main challenges I faced was setting up Android Studio and getting the project up and running. I’ve used other IDEs like **VSCode** and **JetBrains** before, so Android Studio felt a bit heavy at first. There’s so much functionality packed in, but you really need to understand how to navigate it to be productive. It took a bit of time to get familiar with its layout, configurations, and dependencies. Once I understood how Gradle worked, it became easier, but the initial setup felt too much.

2. **Working with Layouts in `activity_main.xml`:**
   Another challenge I encountered was working with the `activity_main.xml` file for layout design. It took me a while to understand how to properly use `TextView` components and position them correctly within the layout. I had to learn how to organize the UI and link the layout elements to the corresponding Java code using `findViewById()`. The trial and error approach with alignment and constraints also helped me realize the importance of layout practices in Android.

3. **Understanding and Modifying the `AndroidManifest.xml`:**
   The `AndroidManifest.xml` file was another area I had to get used to. This file is key for declaring permissions, activities, and services in the app. Initially, I wasn’t clear on which permissions needed to be declared in the manifest and which would be requested at runtime. I struggled with getting the app to run properly because I was unsure of where to add the correct entries and what exactly needed to be changed. Once I understood the structure and purpose of the `AndroidManifest`, it became easier to work with.

4. **Getting Location to Work:**
   One of the most challenging aspects was getting the location feature to work. Location permissions and settings in Android are sensitive, and the way they interact with each other can be confusing. For example, I had to handle **runtime permissions** and also check if location services (like GPS) were enabled on the device. When dealing with the `FusedLocationProviderClient`, it wasn’t always clear when location data was available or how to handle failures effectively. I had to deal with the complexities of using location settings and handling the possible errors when fetching real-time location data.

---
