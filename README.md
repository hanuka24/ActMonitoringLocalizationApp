# ActMonitoringLocalizationApp
Activity monitoring and localization android app for mobile computing course
## Activity Monitoring
### Status
New App design with 3 activities:
1. Main activity (start up): Select either training or monitoring
2. Train activity: Activity patterns walking, standing up, sitting down and idle are monitored
    and the extracted features are saved into one file called "Trainingsdata.txt"
    Current features:
    - mean_x (Mean of x axis)
    - mean_y (Mean of y axis)
    - mean_z (Mean of z axis)
    - min (Min of quadratic sum of all axes)
    - max (Max of quadratic sum of all axes)
    - (fft)
    The current window size is 60 samples.
3. Monitoring activity: At pressing "Start monitoring", one cycle (60 samples) of activity monitoring is executed.
   Current Trainingsset (only 36 samples) gives an accuracy of 75%, but offline computation with more raw data samples gives an accuracy of 95%. Continouts monitoring is also possible by ticking the "Continous monitoring" check-box.

   
### TODO
- Record trainingset for accuary > 90%
- Error handling; App closes handling etc. (App fixed to portrait mode, no need for landscape)
- Improvement of classification: linearization/interpolating?
#### Nice to have:
- Scope of current accelerometer data
- Polishing (pictures etc.)
- Maybe we implement user defined activities

## Localization
### Status
Particle filter implemented
### TODO
- Variable naming
- Polishing (remove Set orientation, add arrow, rename add particle etc)
- getter/setter instead of public members?
- positioning?



