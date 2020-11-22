package com.adgear.trader.oms

import java.sql.DriverManager

import org.olap4j.OlapConnection
import org.olap4j.metadata.{Dimension, Hierarchy, Level}

import scala.jdk.CollectionConverters._

object Main {
  val sep = " | "

  def displayHierarchy(hierarchy: Hierarchy): String = {
    val levels = hierarchy.getLevels.asScala
      .sortBy(_.getDepth)
      .map(displayLevel)

    s"""Hierarchy: 
       |  Default member: ${hierarchy.getDefaultMember}
       |  Levels:         $levels
       |  Dimension:      ${hierarchy.getDimension}""".stripMargin
  }

  def displayLevel(lvl: Level): String = {
    val members = lvl.getMembers.asScala
      .map(_.toString)

    s"""Level:     ${lvl.toString}
       |  Members: ${members.mkString(sep)}""".stripMargin
  }

  def diplayDimension(dim: Dimension): String = {
    s"""Dimension: $dim
       |  Type: ${dim.getDimensionType}""".stripMargin
  }

  def main(args: Array[String]): Unit = {
    Class.forName("mondrian.olap4j.MondrianOlap4jDriver")

    val cnxUrl = "jdbc:mondrian:Jdbc=jdbc:h2:file:~/olap/poc;" +
      "JdbcDrivers=org.h2.Driver;" +
      "JdbcUser=username;" +
      "JdbcPassword=password;" +
      "Catalog=/Users/candre/olap/schema.mondrian.xml"

    val jdbcConnection                 = DriverManager.getConnection(cnxUrl)
    val olapConnection: OlapConnection = jdbcConnection.unwrap(classOf[OlapConnection])

    val cubes = olapConnection.getOlapSchema.getCubes

    val cubeNames = cubes.asMap().asScala
    println(s"Cubes: ${cubeNames.keySet.mkString(sep)}")

    val exampleCube = cubes.get("SteelWheelsSales")
    val measures    = exampleCube.getMeasures.asScala.toList
    val hierarchies = exampleCube.getHierarchies.asScala.toList.map(displayHierarchy)
    val dimensions  = exampleCube.getDimensions.asScala.toList.map(diplayDimension)

    println(s"Measures: ${measures.mkString(sep)}")
    println(s"Hierarchies: ${hierarchies.mkString(sep)}")
    println(s"Dimensions: ${dimensions.mkString(sep)}")
  }
}
