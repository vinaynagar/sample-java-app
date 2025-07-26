# sample-java-app

This is a java application which connects to MySQL Database and run a query.

There is a file in resources folder application.properties in which you can update details of Databasee and application port.

Prerequisite-
Java11 - To compile and run the app.
Maven - To compile and package the app.
MySQL - Database should be up and running before you start the application.


Build and run the application

1. Go to parent path and run below commands
 mvn clean package
 cd target
 ls -ltr


2. Copy jar file to your application folder and run below command to start an application.
   java -jar <Jar Name>
