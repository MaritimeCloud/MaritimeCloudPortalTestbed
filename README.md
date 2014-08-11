MaritimeCloudPortalTestbed
==========================

(Temporar repository until portal code is ready to go public)

A tool that offers Identity & Access Management of the Maritime Cloud Services as 
well as management of services published in Maritime Cloud.

The live system can be found here: TBD

## Software Architecture

The MaritimeCloudPortalTestbed client is a rich client HTML/JS-application with a server side JSON 
webservice API. The server is currently a Spring Boot wrapped standalone Jetty server application.

On the client side we use:

* JavaScript/HTML
* Grunt (for building)
* Twitter Bootstrap (for basic layout)
* AngularJS (for forms and calling webservices)
* JQuery (for some DOM-manipulation)
* HTML5 Application Cache
* Karma (for unit testing)

On the server side we use:

* Java 8
* Maven (for building)
* JPA(Hibernate) (for persistence)
* SpringFramework (for dependency injection)
* Jersey (for JSON-webservices)
* Shiro (for security)
* JUnit (for unit-test)
* Mockito (for mocking)


## Client Architecture Structure

The client application structure tend to organize resources based on features rather than their types in line 
with the Google recommendations for Angular Applications (as outlined in: [Googles 
recommendations](https://docs.google.com/document/d/1XXMvReO8-Awi1EZXAXS4PzDzdNvV6pGcuaF4Q9821Es/pub) ). 
In addition, it leans more towards the DRY'er guidelines by John Papa, as outlined 
in [John Papas Guidelines](http://www.johnpapa.net/angular-app-structuring-guidelines/). Particularly we try to 
limit redundant use of "-controller" in JS-filenames when it is obvious that this is the only kind of JS 
content in a folder. Actually, we take the LIFT guidelines a bit further. Instead of introducing a js-file for 
each controller in a module, we gather them in a single js-file. It also may contain filters and very 
specialized directives. The reasoning is that we want to limit the maintenance of dependencies in the index.html 
file as well as the shared dependencies on module names that will be scattered across many a controller- or 
filter file.   
The rule of thumb, so far, is to have a single js-file in each component named after that component have it and 
define a corresponding angular module like this "mcp.<component name>". If the file gets to big then fall back 
to the more rigid one-file-per-controller rule but share the module name. (All this may of course change again 
in the future.) 

Also, we use users.html instead of user-list.html.

Example:

```
app/
  users/
    user-details.html
    users.html
    users.js
    users_test.js
  organizations/
    ...  
```

## Prerequisites ##

* Java JDK 1.8
* Maven 3.x
* Node.js (Follow the installation instructions at http://nodejs.org)
* Bower (Follow the installation instructions at http://bower.io)

    npm install -g bower

* ( Not in use just yet: )
** Grunt.js (Follow the installation instructions at http://gruntjs.com)

## Initial setup

In order to download front-end dependencies you need to run

    bower install

This will download external dependencies to the folder "src/main/webapp/app/bower"

## Building ##

    mvn clean install

## Testing ##

The frontend uses karma for unit-testing. To launch karma during development, simply run

    karma karma.conf.js

## Launch

The build produces a launchable .war-file in the /target folder. The application can be launched with:

    MaritimeCloudPortalTestbed> java -jar target/service-0.0.1-SNAPSHOT.war

or by using maven:

    MaritimeCloudPortalTestbed> mvn spring-boot:run

A local deployment will setup MaritimeCloudPortalTestbed at the following URL:

    http://localhost:8080/app/index.html

### Login

Currently only a limited set of test users exists. To gain admin rights log in with admin/test. To see an ordinary user log in with 
Tintin/test.


## Instant reload of web resources and running in exploded mode

(This works at least on NetBeans ... not sure for other ides!?)

When using NetBeans as Ide you can easily open the project as a maven project.

To launch the server from inside Netbeans, navigate to the main class 

    java.net.maritimecloud.portal.Application

and launch it. 

(Notice: On MacBooks it's adviced to launch the application in debug-mode, as for some reason the IDE is unable to kill the maven-spawned 
process afterwards.)

Also, currently a durable persistence mechanism is not implemented, and it is therefore necessary to use the in memory based implementation 
based on hashmaps. To launch this version navigate to the test class below and launch it 

    java.net.maritimecloud.portal.ApplicationInMemory


## Netbeans setup ##

Simply open the project as an existing maven-based project. Thats it - no mumbojumbo here ;)


## JavaScript Validation Errors in Netbeans & Eclipse

### Ways to avoid annoying JavaScript Validation Errors in Netbeans:

Navigate to one of the offending scripts and open it. Go to one of the offending lines and click the light-bulb in left margin. 
Choose to suppress warnings from the menu by choosing the folder level that encapsulates the offending scripts.

### Ways to avoid annoying JavaScript Validation Errors in Eclipse:

http://stackoverflow.com/questions/7102299/eclipse-javascript-validation-disabled-but-still-generating-errors


