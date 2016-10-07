resolvers += Resolver.jcenterRepo

scalaVersion := "2.11.8"

val doobieVersion = "0.3.1-M1"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.tpolecat" %% "doobie-core-cats" % doobieVersion,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.iheart" %% "ficus" % "1.2.7",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7"
)
