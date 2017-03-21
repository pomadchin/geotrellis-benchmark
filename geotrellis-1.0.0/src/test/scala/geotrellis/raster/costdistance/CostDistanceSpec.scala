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
import geotrellis.raster.testkit._
import spire.syntax.cfor._
import scaliper._
import java.util.Locale

import geotrellis.spark.util.KryoWrapper

import scala.language.implicitConversions


object CostDistanceBenhmarkFunctions {
  implicit def array2Tile(a: Array[Int]): Tile = {
    val size = math.sqrt(a.length).toInt

    IntArrayTile(a, size, size)
  }

  def asTile(a: Array[Int], cols: Int, rows: Int): Tile =
    IntArrayTile(a, cols, rows)
}

class CostDistanceBenhmark extends Benchmarks with ConsoleReport with Serializable {

  benchmark("CostDistanceBenhmark") {

    run("ESRI example") {
      new Benchmark {
        import CostDistanceBenhmarkFunctions._

        var n: Int = _
        var N: Double = _

        // Example from ESRI
        // http://webhelp.esri.com/arcgisdesktop/9.2/index.cfm?TopicName=Cost_Distance_algorithm
        var costTile: Array[Int] = _
        var points: Seq[(Int, Int)] = _
        var expected: Array[String] = _

        override def setUp() = {
          n = NODATA
          N = Double.NaN
          costTile = Array(
            1,3,4,4,3,2,
            4,6,2,3,7,6,
            5,8,7,5,6,6,
            1,4,5,n,5,1,
            4,7,5,n,2,6,
            1,2,2,1,3,4)
          points = Seq(
            (1,0),
            (2,0),
            (2,1),
            (0,5))
          expected = Array(
            2.0, 0.0, 0.0, 4.0, 6.7, 9.2,
            4.5, 4.0, 0.0, 2.5, 7.5, 13.1,
            8.0, 7.1, 4.5, 4.9, 8.9, 12.7,
            5.0, 7.5, 10.5,  N, 10.6, 9.2,
            2.5, 5.7, 6.4,   N, 7.1, 11.1,
            0.0, 1.5, 3.5, 5.0, 7.0, 10.5).map(i => " %04.1f ".formatLocal(Locale.ENGLISH, i))
        }

        def run() = {
          val cd = CostDistance(costTile, points)
          val d = cd.toArrayDouble()
          val strings = d.map(i => " %04.1f ".formatLocal(Locale.ENGLISH, i))
          for(i <- strings.indices) {
            println(s"${strings(i)} == ${expected(i)}: ${strings(i) == expected(i)}")
          }
        }
      }
    }

    run("GRASS example") {
      new Benchmark {
        import CostDistanceBenhmarkFunctions._
        // Example from GRASS
        // http://grass.osgeo.org/grass64/manuals/r.cost.html
        var costTile: Tile = _

        var points: Seq[(Int, Int)] = _

        var expected: Array[Int] = _

        override def setUp() = {
          costTile = asTile(Array(
            2 , 2 , 1 , 1 , 5 , 5 , 5 ,
            2 , 2 , 8 , 8 , 5 , 2 , 1 ,
            7 , 1 , 1 , 8 , 2 , 2 , 2 ,
            8 , 7 , 8 , 8 , 8 , 8 , 5 ,
            8 , 8 , 1 , 1 , 5 , 3 , 9 ,
            8 , 1 , 1 , 2 , 5 , 3 , 9), 7, 6)

          points = Seq((5,4))

          expected = Array(
            22,21,21,20,17,15,14,
            20,19,22,20,15,12,11,
            22,18,17,18,13,11, 9,
            21,14,13,12, 8, 6, 6,
            16,13, 8, 7, 4, 0, 6,
            14, 9, 8, 9, 6, 3, 8)
        }

        def run() = {
          val cd = CostDistance(costTile, points)
          val d = cd.toArrayDouble()
          val values = d.toSeq.map(i => (i + 0.5).toInt)
          // println(values.toSeq)
          // println(expected.toSeq)

          for(i <- values.indices) {
            println(s"${values(i)} == ${expected(i)}: ${values(i) == expected(i)}")
          }
        }
      }
    }

    def print(d: DoubleArrayTile): Unit =
      println(d.array.toList.map(i => " %04.1f ".formatLocal(Locale.ENGLISH, i)).grouped(d.cols).map(_.mkString(",")).mkString("\n"))

  }
}
