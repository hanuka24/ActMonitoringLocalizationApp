# Activity Monitoring and Localization App

## Current Status

When pressing "New file", a new file called ActivityX.txt is created on the SD card 
(X is incremented each time).

When pressing "Start Activity" the accelerometer is enabled and the data is written to ActivityX.txt until 
"Stop Activity" is pressed.

## Next Steps
The app might crash if the screen rotation changes since the app is stopped then
We should save and restore the apps state to avoid that.

Maybe we add a field for the filename 

