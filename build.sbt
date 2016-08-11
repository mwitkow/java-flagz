import sbt.Keys._
import sbt.Tests

name := "flagz-root"

publish :=() // don't publish the root aggregate project.
publishTo.in(Global) := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

organization.in(Global) := "org.flagz"

version.in(Global) := "2.2.0"

licenses.in(Global) := Seq("MIT License" -> url("https://github.com/sbt/sbt-assembly/blob/master/LICENSE"))

resolvers.in(Global) := Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Maven Central Server" at "http://repo1.maven.org/maven2")

credentials.in(Global) ++= (Path.userHome / ".ivy2" * "*credentials").get.map(Credentials(_))

// top level one, because SBT is magic.
checkstyleConfigLocation := CheckstyleConfigLocation.File("checkstyle.xml")

val sharedSettings: Seq[Def.Setting[_]] = Seq(
  // Point at a checkstyle config.
  checkstyleConfigLocation := CheckstyleConfigLocation.File("checkstyle.xml"),
  // Fail the build if any checkstyles are exceeding warning.
  checkstyleSeverityLevel := Some(CheckstyleSeverityLevel.Warning),
  // Disable javadoc building and compilation, we use Markdown:
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq.empty
)

val pureJavaSettings: Seq[Def.Setting[_]] = Seq(
  // Disable all of the Scala nonsense.
  publishMavenStyle := true,
  autoScalaLibrary := false,
  crossPaths := false,
  // Make javac compiler complain about deprecation and bad casts.
  javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
  // Run tests using JUnit, but exclude Integration tests by default.
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v",
      "--exclude-categories=org.flagz.IntegrationTestCategory"),
  libraryDependencies += "junit" % "junit" % "4.12" % "test",
  libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"
)


lazy val core = project.in(file("flagz-java"))
    .settings(name := "flagz")
    .settings(sharedSettings: _*)
    .settings(pureJavaSettings: _*)
    .settings(libraryDependencies += "org.reflections" % "reflections" % "0.9.10")
    .settings(libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.14")
    .settings(libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.14" % "test")
    .settings(libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test")


lazy val samples = project.in(file("samples"))
    .settings(name := "flagz-samples")
    .settings(scalaVersion := "2.11.4")
    .settings(sharedSettings: _*)
    .dependsOn(core)
    .dependsOn(scala)
    .dependsOn(etcd)


lazy val scala = project.in(file("flagz-scala"))
    .settings(name := "flagz-scala")
    .settings(scalaVersion := "2.11.4")
    .settings(sharedSettings: _*)
    .settings(libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % "test")
    .dependsOn(core)

lazy val etcd = project.in(file("flagz-etcd"))
    .settings(name := "flagz-etcd")
    .settings(sharedSettings: _*)
    .settings(pureJavaSettings: _*)
    .settings(libraryDependencies += "org.mousio" % "etcd4j" % "2.10.0")
    // Make sure we depend on test stuff as well for the IntegrationTestCategory.
    .dependsOn(core  % "compile->compile;test->test")

pomExtra := (<licenses>
  <license>
    <url>http://www.opensource.org/licenses/mit-license.php</url>
  </license>
</licenses>)
