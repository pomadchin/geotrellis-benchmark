/*
 * Copyright 2016 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmark.geotrellis.raster.costdistance

import geotrellis.raster._
import geotrellis.raster.costdistance._
import geotrellis.raster.io.geotiff.SinglebandGeoTiff

import scaliper._

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.language.implicitConversions


object CostDistanceBenchmarkFunctions {
  implicit def array2Tile(a: Array[Int]): Tile = {
    val size = math.sqrt(a.length).toInt

    IntArrayTile(a, size, size)
  }

  def asTile(a: Array[Int], cols: Int, rows: Int): Tile =
    IntArrayTile(a, cols, rows)

  val n = NODATA
  val N = Double.NaN

  val esriTile: Tile = Array(
    1, 3, 4, 4, 3, 2,
    4, 6, 2, 3, 7, 6,
    5, 8, 7, 5, 6, 6,
    1, 4, 5, n, 5, 1,
    4, 7, 5, n, 2, 6,
    1, 2, 2, 1, 3, 4
  )

  val grassTile = asTile(Array(
    2 , 2 , 1 , 1 , 5 , 5 , 5 ,
    2 , 2 , 8 , 8 , 5 , 2 , 1 ,
    7 , 1 , 1 , 8 , 2 , 2 , 2 ,
    8 , 7 , 8 , 8 , 8 , 8 , 5 ,
    8 , 8 , 1 , 1 , 5 , 3 , 9 ,
    8 , 1 , 1 , 2 , 5 , 3 , 9), 7, 6)

  val aspectTiff = SinglebandGeoTiff("geotrellis/src/test/resources/data/aspect-tif.tif").tile
  val aspectTiff256 = aspectTiff.crop(0, 0, 256, 256)
  val aspectTiff512 = aspectTiff.crop(0, 0, 512, 512)

}

class CostDistanceBenchmark extends Benchmarks with ConsoleReport with Serializable {

  benchmark("CostDistanceBenchmark") {
    run("benchmark with one point on ESRI example (6 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = esriTile
          points = Seq((1, 1))
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with half of all points on ESRI example (6 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = esriTile
          points =
            (0 until costTile.cols / 2) flatMap { c =>
              (0 until costTile.rows / 2) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    for (i <- List(3, 10, 100, 1000, 10000, 100000, 1000000)) {
      run(s"benchmark with rnd 1/$i of all points on ESRI example (6 x 6)") {
        new Benchmark {
          import CostDistanceBenchmarkFunctions._

          var costTile: Tile = _
          var points: Seq[(Int, Int)] = _
          override def setUp() = {
            costTile = esriTile
            points =
              (0 until costTile.size / i) map { _ =>
                (ThreadLocalRandom.current().nextInt(0, costTile.cols), ThreadLocalRandom.current().nextInt(0, costTile.rows))
              }
          }
          def run() = {
            CostDistance(costTile, points)
          }
        }
      }
    }

    run("benchmark with all points on ESRI example (6 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = esriTile
          points =
            (0 until costTile.cols) flatMap { c =>
              (0 until costTile.rows) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with one point on GRASS example (7 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = grassTile
          points = Seq((1, 1))
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with half of all points on GRASS example (7 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = grassTile
          points =
            (0 until costTile.cols / 2) flatMap { c =>
              (0 until costTile.rows / 2) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    for (i <- List(3, 10, 100, 1000, 10000, 100000, 1000000)) {
      run(s"benchmark with rnd 1/$i of all points on GRASS example (7 x 6)") {
        new Benchmark {
          import CostDistanceBenchmarkFunctions._

          var costTile: Tile = _
          var points: Seq[(Int, Int)] = _
          override def setUp() = {
            costTile = grassTile
            points =
              (0 until costTile.size / i) map { _ =>
                (ThreadLocalRandom.current().nextInt(0, costTile.cols), ThreadLocalRandom.current().nextInt(0, costTile.rows))
              }
          }
          def run() = {
            CostDistance(costTile, points)
          }
        }
      }
    }

    run("benchmark with all points on GRASS example (7 x 6)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = grassTile
          points =
            (0 until costTile.cols) flatMap { c =>
              (0 until costTile.rows) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with one point on aspect-tif.tif (256 x 256)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff256
          points = Seq((1, 1))
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with half of all points on aspect-tif.tif (256 x 256)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff256
          points =
            (0 until costTile.cols / 2) flatMap { c =>
              (0 until costTile.rows / 2) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    for (i <- List(3, 10, 100, 1000, 10000, 100000, 1000000)) {
      run(s"benchmark with rnd 1/$i of all points on aspect-tif.tif (256 x 256)") {
        new Benchmark {
          import CostDistanceBenchmarkFunctions._

          var costTile: Tile = _
          var points: Seq[(Int, Int)] = _
          override def setUp() = {
            costTile = aspectTiff256
            points =
              (0 until costTile.size / i) map { _ =>
                (ThreadLocalRandom.current().nextInt(0, costTile.cols), ThreadLocalRandom.current().nextInt(0, costTile.rows))
              }
          }
          def run() = {
            CostDistance(costTile, points)
          }
        }
      }
    }

    run("benchmark with all points on aspect-tif.tif (256 x 256)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff256
          points =
            (0 until costTile.cols) flatMap { c =>
              (0 until costTile.rows) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with one point on aspect-tif.tif (512 x 512)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff512
          points = Seq((1, 1))
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with half of all points on aspect-tif.tif (512 x 512)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff512
          points =
            (0 until costTile.cols / 2) flatMap { c =>
              (0 until costTile.rows / 2) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    for (i <- List(3, 10, 100, 1000, 10000, 100000, 1000000)) {
      run(s"benchmark with rnd 1/$i of all points on aspect-tif.tif (512 x 512)") {
        new Benchmark {
          import CostDistanceBenchmarkFunctions._

          var costTile: Tile = _
          var points: Seq[(Int, Int)] = _
          override def setUp() = {
            costTile = aspectTiff512
            points =
              (0 until costTile.size / i) map { _ =>
                (ThreadLocalRandom.current().nextInt(0, costTile.cols), ThreadLocalRandom.current().nextInt(0, costTile.rows))
              }
          }
          def run() = {
            CostDistance(costTile, points)
          }
        }
      }
    }

    run("benchmark with all points on aspect-tif.tif (512 x 512)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff512
          points =
            (0 until costTile.cols) flatMap { c =>
              (0 until costTile.rows) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with one point on aspect-tif.tif (1500 x 1350)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff
          points = Seq((1, 1))
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    run("benchmark with half of all points on aspect-tif.tif (1500 x 1350)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff
          points =
            (0 until costTile.cols / 2) flatMap { c =>
              (0 until costTile.rows / 2) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }

    for (i <- List(3, 10, 100, 1000, 10000, 100000, 1000000)) {
      run(s"benchmark with rnd 1/$i of all points on aspect-tif.tif (1500 x 1350)") {
        new Benchmark {
          import CostDistanceBenchmarkFunctions._

          var costTile: Tile = _
          var points: Seq[(Int, Int)] = _
          override def setUp() = {
            costTile = aspectTiff
            points =
              (0 until costTile.size / i) map { _ =>
                (ThreadLocalRandom.current().nextInt(0, costTile.cols), ThreadLocalRandom.current().nextInt(0, costTile.rows))
              }
          }
          def run() = {
            CostDistance(costTile, points)
          }
        }
      }
    }

    run("benchmark with all points on aspect-tif.tif (1500 x 1350)") {
      new Benchmark {
        import CostDistanceBenchmarkFunctions._

        var costTile: Tile = _
        var points: Seq[(Int, Int)] = _
        override def setUp() = {
          costTile = aspectTiff
          points =
            (0 until costTile.cols) flatMap { c =>
              (0 until costTile.rows) map { r =>
                (c, r)
              }
            }
        }
        def run() = {
          CostDistance(costTile, points)
        }
      }
    }
  }
}
