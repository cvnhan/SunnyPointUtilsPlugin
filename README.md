# SunnyPointUtilsPlugin
SunnyPoint utils plugin: pull db file and show on PC

# Required: run cmd to test 
Make sure setup Environment Variables for:

1. jar (jdk\bin)
2. g++ (mingw\bin)
3. adb (adb portable)

Flow: 

1 With android unroot device:
+ Using androidaccessdb to move db file to sdcard with name main.db
+ Using SynnyPointUtilsPlugin to pull it to pc and show with sqlitebrowser

2 With android rooted device:
+ Using package name to access db file and pull it to pc and show with sqlitebrowser

