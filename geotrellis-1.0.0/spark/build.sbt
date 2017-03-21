name := "geotrellis-spark-benchmark"
libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-spark" % "1.0.0",
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.hadoop" % "hadoop-client" % "2.7.3"
)

javaOptions ++= List(
  "-Xmx6G",
  "-XX:MaxPermSize=384m"
)

fork := true
