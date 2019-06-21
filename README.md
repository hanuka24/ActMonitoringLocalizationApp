# ActMonitoringLocalizationApp
Activity monitoring and localization android app for mobile computing course

## App
The app consists of four activities:
1. Main activity (start up): Select either training or monitoring for activity monitoring, or start localization
2. Train activity: Train motions for activity monitoring
3. Monitoring activity: Display current activity
4. Localization activity: Indoor localization in ITI buildings' first floor

### Activity Monitoring
#### Status
- Train activity: Activity patterns walking, standing up, sitting down and idle are monitored
    and the extracted features are saved into one file called "Trainingsdata.txt"
    Current features:
    - mean_x (Mean of x axis)
    - mean_y (Mean of y axis)
    - mean_z (Mean of z axis)
    - min (Min of quadratic sum of all axes)
    - max (Max of quadratic sum of all axes)
    - (fft)
    The current window size is 60 samples.
*´- Monitoring activity: At pressing "Start monitoring", one cycle (60 samples) of activity monitoring is executed.
   Current Trainingsset (only 36 samples) gives an accuracy of 80%, but offline computation with more raw data samples gives an accuracy of 95%. Continouts monitoring is also possible by ticking the "Continous monitoring" check-box.

### Localization
#### Status
- Indoor localization works quite well, with limitations due to inaccurate orientation sensing.

## Report
The report can be found as .tex or .pdf file in the Report folder and contains information about both tasks (activity monitoring and localization)

## KNN 
In the KNN folder, a python script for offline activity classification can be found.


## Data
Contains a few sets of trainingsdata for activity monitoring



