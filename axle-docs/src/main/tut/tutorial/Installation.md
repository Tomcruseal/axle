---
layout: page
title: Installation
permalink: /tutorial/installation/
---

Axle as a dependency of an SBT project.

Install SBT
-----------

See [SBT](http://www.scala-sbt.org/)

Create SBT Project
------------------

```bash
mkdir demo
cd demo
```

JBLAS
-----

On apt-based operating systems:

```bash
sudo apt-get install libgfortran3
```

Create a `build.sbt` file

```
name := "demo"

version := "0.1-SNAPSHOT"

organization := "org.acme"

scalaVersion := "2.11.8"

// resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.axle-lang" %% "axle-core"  % "releaseVersion",
  "org.axle-lang" %% "axle-wheel" % "releaseVersion",
  "org.axle-lang" %% "axle-jblas" % "releaseVersion",
  "org.axle-lang" %% "axle-joda"  % "releaseVersion"
)
```

The Axle jars are compiled with several additional dependencies in `provided` scope,
meaning that they are compiled and packaged with the expectation that the user of the Axle
jars will explicitly provide those dependencies.

See the project's current (build.sbt)[https://github.com/axlelang/axle/blob/master/build.sbt]
file for up to date version information.

As of version 0.4.0 the full list of dependencies is below.
Add this section to your `build.sbt` file to pull them all in to the `demo` project:

```
libraryDependencies ++= Seq(

  // needed by axle-core
  "org.jogamp.gluegen"     % "gluegen-rt-main"    % "2.3.2",
  "org.jogamp.jogl"        % "jogl-all-main"      % "2.3.2",
  "net.sf.jung"            % "jung-visualization" % "2.1",
  "net.sf.jung"            % "jung-algorithms"    % "2.1",
  "net.sf.jung"            % "jung-api"           % "2.1",
  "net.sf.jung"            % "jung-graph-impl"    % "2.1",
  //"net.sf.jung"          % "jung-io"            % "2.1",

  // for animations
  "io.monix"               %% "monix-reactive"    % "2.3.0",
  "io.monix"               %% "monix-cats"        % "2.3.0",

  // needed by axle-jblas
  "org.jblas"              % "jblas"              % "1.2.4",

  // needed by axle-joda
  "joda-time"              % "joda-time"          % "2.9.4",
  "org.joda"               % "joda-convert"       % "1.8.1"
)
```

Next Steps
----------

Run `sbt console` to launch the Scala REPL with the Axle jars in the classpath.
Axle works well interactively -- especially during prototyping, debugging, and testing.
Any of the [Axle tutorials](/tutorial/) can be copied and pasted into the REPL.

To start writing code, do `mkdir -p src/main/scala/org/acme/demo`, and add your code there.

Releases
--------

Version snapshotVersion is hosted on the [Sonatype snapshot repo](https://oss.sonatype.org/content/repositories/snapshots).
The schedule for publishing snapshot versions is irregular.
The full dependency is:

```
"org.axle-lang" %% "axle-core" % "snapshotVersion"
```

releaseVersion is the most recent released version:

```
"org.axle-lang" %% "axle-core" % "releaseVersion"
```

In addition to `axle-core` the artifacts `axle-jblas`, `axle-joda`, and `axle-wheel`
are also published to Sonatype.

See the [Road Map](/road_map/) for more information on the release schedule.
