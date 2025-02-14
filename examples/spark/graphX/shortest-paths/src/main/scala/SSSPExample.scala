/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * modified by amit
 * Shows the use of a Pregel:
 *
 * Pregel (a portmanteu of the words Parallel, Graph, and Google) is a data flow paradigm and 
 * system for large-scale graph processing created at Google to solve problems that are hard 
 * or expensive to solve using only the MapReduce framework.
 */


import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.sql.SparkSession

/**
 * An example use the Pregel operator to express computation
 * such as single source shortest path
 */
object SSSPExample {
  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    val spark = SparkSession
      .builder
      .appName("shortest-paths")
      .getOrCreate()
    val sc = spark.sparkContext

    // $example on$
    // A graph with edge attributes containing distances
    val graph: Graph[Long, Double] =
      GraphGenerators.logNormalGraph(sc, numVertices = 100).mapEdges(e => e.attr.toDouble)

    println()
    println("Number of vertices = " + graph.numVertices)
    println("Number of edges = " + graph.numEdges)
    println()

    //println()
    //println(" --- Initial Graph ---")
    //graph.vertices.foreach(println(_))
    //graph.edges.foreach(println(_))
    //println()

    val sourceId: VertexId = 0 // The ultimate source
    // Initialize the graph such that all vertices except the root have distance infinity.
    //
    val initialGraph = graph.mapVertices((id, _) =>
        if (id == sourceId) 0.0 else Double.PositiveInfinity)

    val sssp = initialGraph.pregel(Double.PositiveInfinity)(
      (id, dist, newDist) => math.min(dist, newDist), // Vertex Program
      triplet => {  // Send Message
        if (triplet.srcAttr + triplet.attr < triplet.dstAttr) {
          Iterator((triplet.dstId, triplet.srcAttr + triplet.attr))
        } else {
          Iterator.empty
        }
      },
      (a, b) => math.min(a, b) // Merge Message
    )
    println("---- final distances for each vertex from the source vertex 42 ----");
    println(sssp.vertices.collect.mkString("\n"))

    // show how to print vertices 0-99
    val sortedVertices = sssp.vertices.sortByKey()
    println()
    println(" --- Final Vertex List ---")
    sortedVertices.collect().foreach(println(_))
    println()

    spark.stop()
  }
}
