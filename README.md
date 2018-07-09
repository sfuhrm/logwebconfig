Log Web Config
===================
![Travis CI Status](https://travis-ci.org/sfuhrm/logwebconfig.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/sfuhrm/logwebconfig/badge.svg)](https://coveralls.io/github/sfuhrm/logwebconfig) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sfuhrm/logwebconfig/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sfuhrm/logwebconfig) 

A simple runtime configuration REST configuration of the Log4j2 logging facility.

## Including it in your projects

The recommended way of including the library into your project is adding the
following Apache Maven dependency:

---------------------------------------

```xml
<dependency>
    <groupId>de.sfuhrm</groupId>
    <artifactId>logwebconfig</artifactId>
    <version>0.0.1</version>
</dependency>
```

---------------------------------------

At program start you need to call the service once so the
server starts up:

---------------------------------------

```java
import de.sfuhrm.logwebconfig.LogWebConfig;

public class MyApp {
    public static void main(String args[]) {
        LogWebConfig.start();
        
        // your program comes here
    }
}
```

---------------------------------------

## Startup time: Configuring the server

Configuration of the server is done using Java system properties.
By default the server listens on TCP/IP port 19293.

The following properties are there for configuration:

* **LOGWEBCONFIG_PORT**: The TCP/IP port for the HTTP-server to listen to. Example value is 19293 which is also the default port.
* **LOGWEBCONFIG_HOST**: The IP address to bind the listening socket to. Defaults to "127.0.0.1" which is only reachable from localhost.
This can be used to restrict the accessibility of the configuration from outside. Exposing the service to the internet by listening to all addresses ("0.0.0.0")
might be a security risk because you can put the root logging level of your application to debug.
* **LOGWEBCONFIG_ENABLE**: Whether to enable the server. Defaults to true.

## Run time: Configuring log4j2

Log4j2 is configured by simply issueing PUT requests with the logger seen as a
resource and the log level as a request parameter.

Example for setting the root logger level:

---------------------------------------

```
curl -X PUT http://localhost:19293/log4j2//level?level=WARN
```

---------------------------------------

Example for setting the logger level for class 'com.company.my.Class':

---------------------------------------
```
curl -X PUT http://localhost:19293/log4j2/com.company.my.Class/level?level=WARN
```

---------------------------------------

## Versions

The version numbers are chosen according to the
[semantic versioning](https://semver.org/) schema.
Especially major version changes come with breaking API
changes.

## Author

Written 2018 by Stephan Fuhrmann. You can reach me via email to s (at) sfuhrm.de

## License

Copyright 2018 Stephan Fuhrmann

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
