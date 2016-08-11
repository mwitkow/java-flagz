
# FlagZ - modern flag library for Java/Scala

[![Travis Build](https://travis-ci.org/mwitkow/java-flagz.svg)](https://travis-ci.org/mwitkow/java-flagz)
[![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A modern, annotation-based flags library for Java and Scala that's tailored for large codebases.


## Key features

 * No more passing around config classes, and punching-through parameters.
 * Like [args4j](http://args4j.kohsuke.org/) or [JCommander](http://jcommander.org/) uses `@FlagInfo` annotation-based mapping of flag names to class fields.
 * *Unlike* [args4j](http://args4j.kohsuke.org/) or [JCommander](http://jcommander.org/) allows flags to be specified *anywhere* on the classpath.
 * Support for simple types, e.g. `Boolean`, `Integer`, `String`, `Double`...
 * Support for generic container types, e.g. `List<String>`, `Map<String, Integer>`, `Set<Double>`
 * All flags are *thread-safe* and dynamically modifiable at runtime through:
    - JMX MBeans - a standard Java mechanism for server debugging/tuning - see [`JmxSampleApp`](samples/src/main/java/org/flagz/samples/JmxSampleApp.java) example 
    - [etcd](https://coreos.com/etcd/docs/latest/getting-started-with-etcd.html) - a distributed key-value store, allowing for multiple servers to have their dynamic flags changed in sync - see [`EtcdSampleApp`](samples/src/main/java/org/flagz/samples/EtcdSampleApp.java)
 * Compatibility with existing `System.properties`-based libraries through [`@FlagProperty`](flagz-java/src/main/java/org/flagz/FlagProperty.java) annotation that syncs a flag with a property name.
 * `withValidator` - All flags can have a set of validators attached, that prevent bad values (e.g. out of range) from being set.
 * `withNotifier` - All flags have callabacks that are triggered when flags are modified dynamically.
 * Extensible - just extend [`FlagField`](flagz-java/src/main/java/org/flagz/FlagField.java) and define your own types, e.g. JSON flags, protobuf flags.
 * Scala support 
 

## Paint me a code picture

Let's say your project is more than just a simple CLI utility and consists of multiple packages.

You can define parameters that relate to given functionality *inside* the relevant package.
```java
package com.example.rpc.client
...

public class MyRpcClientFactory {
  @FlagInfo(name="rpc_client_max_threadpool_size", help="Max threadpool size for RPC client callbacks")
  private static final Flag<Integer> maxThreadPool = Flagz.valueOf(10);
  
  @FlagInfo(name="rpc_client_default_timeout_s", help="Default timeout (seconds) for RPCs.")
  private static final Flag<Double> defaultRpcTimeoutSec = Flagz.valueOf(1.5);
  ...
}
```

All `@FlagInfo`-annotated fields on the classpath will automatically be discovered. Let's say that the server's main 
looks like:


```java
package com.example.superserver
...

public class Main {
  @FlagInfo(name="daemonize", altName="d", help="Whether to fork off the process,")
  private static final Flag<Boolean> daemonize = Flagz.valueOf(false);

  @FlagInfo(name="bind_addr", help="Bind addresses to listen on")
  private static final Flag<List<String>> bindList = Flagz.valueOf(ImmutableList.of("0.0.0.0:80"));

  ...
  public static void main(String[] args) {
    Flagz.parse(args);
    ...
    for (bind in bindList.Get()) {
      server.BindTo(bind);
    }
    if (daemonize.Get()) {
      server.Fork()
    }
    ...
  }
  
  ...
}
```

This means you can start the server as:
  
    $ java com.example.superserver.Main --rpc_client_max_threadpool_size=15 -d
    
This will start the server in daemon mode, and change the  the `maxThreadPool` field in `MyRpcClientFactory` without 
the need for punching through massive configuration objects.


## Installing

### Maven Packages

To use this flag library, include this in your `pom.xml`:

```xml
<repositories>
  ...
  <repository>
    <id>mwitkow-github-repo</id>
    <url>https://raw.github.com/mwitkow/maven-repos/master</url>
  </repository>
  ...
</repositories>
<dependencies>
  ...
  <dependency>
    <groupId>org.flagz</groupId>
    <artifactId>flagz</artifactId>
    <version>2.2</version>
  </dependency>
  ...
</dependencies>
```    

### Manual compilation and local publishing

Obviously git-clone this repo.

Then, do:

    $ sbt publish

This should create the relevant `.jar` files in the `target` directories of each project, e.g. `flagz-java/target` and
`flagz-scala/target`.

Copy these to your application, or alternatively publish the artifacts locally

    $ sbt publish-local
    
This will populate your local Ivy cache (the Maven repo cache) with newly constructed artifacts.

## Status

At Improbable we use `flagz-etcd` and `flagz-scala` to dynamically reconfigure our simulation runtime environment, 
allowing our engineers to quickly iterate on tuning parameters and launch new functionality hidden behind flags.

As such, the code is considered **production quality**.


## License and Copyright

`flagz` is released under the MIT License. See the [LICENSE](LICENSE) file for details.

The project was completely written by [@mwitkow](https://github.com/mwitkow)'s as a thread-safe and dynamic
 version of [kennyyu/flags](https://github.com/kennyyu/flags), with only the public interfaces remaining from the old project.
