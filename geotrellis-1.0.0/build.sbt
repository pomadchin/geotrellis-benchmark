name := "geotrellis-benchmark"

libraryDependencies ++= Seq(
  "org.locationtech.geotrellis" %% "geotrellis-raster"         % "1.0.0",
  "org.locationtech.geotrellis" %% "geotrellis-raster-testkit" % "1.0.0",
  "org.locationtech.geotrellis" %% "geotrellis-proj4"          % "1.0.0",
  "org.locationtech.geotrellis" %% "geotrellis-vector"         % "1.0.0",
  "org.locationtech.geotrellis" %% "geotrellis-vectortile"     % "1.0.0",
  "org.locationtech.geotrellis" %% "geotrellis-spark"          % "1.0.0"
)
