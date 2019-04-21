# ActMonitoringLocalizationApp
Activity monitoring and localization android app for mobile computing course

## Status
New App design with 3 activities:
1. Main activity (start up): Select either training or monitoring
2. Train activity: Activity patterns walking, standing up, sitting down are monitored
    and the extracted features are saved into one file called "Trainingsdata.txt"
3. Monitoring activity: At pressing "Start monitoring", one cycle of activity monitoring is executed.
   It works basically, but better trainings data and more sophisticated feature extracation would improve perfomance.
   
## TODO
- Polishing (Buttons color when pressed, general design
- Error handling; App closes handling etc.
- Improvement of classification: knn refactoring, better feature extraction
- Continous monitoring
- Deletion of trainingsdata, last trainings sample etc.