
# Java build tool daemons

Testing running an *interactive process* (requiring terminal user input) with gradle/maven/sbt/mill daemon.  
This is the app in question:
```java
package org.example;

import java.io.*;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Please enter: ");
        var reader = new BufferedReader(new InputStreamReader(System.in));
        var input = reader.readLine();
        System.out.println("You entered: " + input);
    }
}

```

Seems like most of them try to run it within the daemon itself, which seems problematic, see below.  
**Only sbt seems to handle it properly!**



A better approach maybe would be to run it within the client itself.  
The server would only shoot a message to the client with command line args.  
This also offloads the server, and implementing watch/interrupt feels easier.

POC: https://github.com/sake92/mill-client-server-poc

----------
## Maven daemon

```sh
PS D:\projects\tmp\java-build-tools\maven> mvnd compile
...
PS D:\projects\tmp\java-build-tools\maven> mvnd exec:java
[INFO] Processing build on daemon 226ec674
[INFO] Scanning for projects...
[INFO] BuildTimeEventSpy is registered.
[INFO]
[INFO] Using the SmartBuilder implementation with a thread count of 7
[INFO]
[INFO] ---------------------------< ba.sake:maven >----------------------------
[INFO] Building maven 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- exec:1.4.0:java (default-cli) @ maven ---
[INFO] [stdout] Please enter:
```

The input is blocked at this point.  
But at least you can Ctrl+C it.

Of course, `mvn exec:java` works fine.

-----------
## Gradle daemon

```sh
PS D:\projects\tmp\java-build-tools\gradle> .\gradlew run
Reusing configuration cache.

> Task :app:run
Please enter:
You entered: null

BUILD SUCCESSFUL in 1s
2 actionable tasks: 2 executed
Configuration cache entry reused.
```

Gradle daemon conveniently sets your input as `null˙.  
Obviously.

Interestingly the no-daemon version doesn't work either!!! :oooooo
```sh
PS D:\projects\tmp\java-build-tools\gradle> ./gradlew --no-daemon app:run
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.12/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build
Reusing configuration cache.

> Task :app:run
Please enter:
You entered: null

BUILD SUCCESSFUL in 5s
2 actionable tasks: 1 executed, 1 up-to-date
Configuration cache entry reused.
```

---------
### Sbt daemon

```sh
PS D:\projects\tmp\java-build-tools\sbt> sbt --client run
[info] Updated file D:\projects\tmp\java-build-tools\sbt\project\build.properties: set sbt.version to 1.9.7
[info] welcome to sbt 1.9.7 (Eclipse Adoptium Java 21.0.1)
[info] loading project definition from D:\projects\tmp\java-build-tools\sbt\project
[info] loading settings for project sbt from build.sbt ...
[info] set current project to sbt (in build file:/D:/projects/tmp/java-build-tools/sbt/)
[info] compiling 1 Java source to D:\projects\tmp\java-build-tools\sbt\target\scala-2.12\classes ...
[info] running org.example.App
Please enter:
gdfgdf
You entered: gdfgdf
[success] Total time: 4 s, completed 5. jan 2025. 10:00:08
```
Sbt daemon works !

Although if you run 2 commands it will block the second one:
```sh
PS D:\projects\tmp\java-build-tools\sbt> sbt --client run
[info] entering *experimental* thin client - BEEP WHIRR
[info] terminate the server with `shutdown`
> run
```


---------
## Mill daemon


```sh
PS D:\projects\tmp\java-build-tools\mill> ./mill app.run
[build.mill-64/68] compile
[build.mill-64] [info] compiling 1 Scala source to D:\projects\tmp\java-build-tools\mill\out\mill-build\compile.dest\classes ...
[build.mill-64] [info] done compiling
[47/55] app.compile
[47] [info] compiling 1 Java source to D:\projects\tmp\java-build-tools\mill\out\app\compile.dest\classes ...
[47] [info] done compiling
[55/55] app.run
[55] Please enter:
[55] You entered: null
[55/55] ============================= app.run ============================ 5s
```

Mill daemon also conveniently sets your input as `null˙.  
Obviously.

The workaround is to use mill in interactive mode: `./mill -i app.run`

