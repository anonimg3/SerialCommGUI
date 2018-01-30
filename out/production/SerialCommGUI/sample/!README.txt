- install and configure minicom: https://developer.ridgerun.com/wiki/index.php/Setting_up_Minicom_Client_-_Ubuntu
- sudo apt-get install librxtx-java 
- add this line in VM options: -Djava.library.path="/usr/lib/jni/"

After build artifacts:
- java -Djava.library.path="/usr/lib/jni/" -jar SerialCommGUI.jar
