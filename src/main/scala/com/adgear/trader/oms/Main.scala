package com.adgear.trader.oms

import java.sql.DriverManager
import java.util.Formatter

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
      "Catalog=/Users/c.andre/olap/schema.mondrian.xml"

    val jdbcConnection                 = DriverManager.getConnection(cnxUrl)
    val olapConnection: OlapConnection = jdbcConnection.unwrap(classOf[OlapConnection])

    val cubes = olapConnection.getOlapSchema.getCubes

    //    val cubeNames = cubes.asMap().asScala
    //    println(s"Cubes: ${cubeNames.keySet.mkString(sep)}")
    //
    //    val exampleCube = cubes.get("SteelWheelsSales")
    //    val measures    = exampleCube.getMeasures.asScala.toList
    //    val hierarchies = exampleCube.getHierarchies.asScala.toList.map(displayHierarchy)
    //    val dimensions  = exampleCube.getDimensions.asScala.toList.map(diplayDimension)
    //
    //    println(s"Measures: ${measures.mkString(sep)}")
    //    println(s"Hierarchies: ${hierarchies.mkString(sep)}")
    //    println(s"Dimensions: ${dimensions.mkString(sep)}")
    //    println()

    val mdx =
      """SELECT
        | { [Measures].[Sales], [Measures].[Quantity], [Measures].[Average Price Per Item] } ON COLUMNS,
        | { [Product].Children } ON ROWS,
        | { [Time].Children } ON 2
        |FROM [SteelWheelsSales]""".stripMargin

    val cellSet = olapConnection.createStatement().executeOlapQuery(mdx)

    val columns = cellSet.getAxes.get(0).asScala
    val rows    = cellSet.getAxes.get(1).asScala
    val years   = cellSet.getAxes.get(2).asScala

    val cells = for {
      row  <- rows
      col  <- columns
      year <- years
    } yield (
      cellSet.getCell(col, row, year),
      row.getMembers.asScala.toList,
      col.getMembers.asScala.toList,
      year.getMembers.asScala.toList
    )

    val leftAlignFormat = " %-40s | %-30s | %-30s | %-30s"
    val titles = {
      val lines = new Formatter().format(
        leftAlignFormat,
        List.fill(40)("-").mkString,
        List.fill(30)("-").mkString,
        List.fill(30)("-").mkString,
        List.fill(30)("-").mkString
      )

      lines + System.lineSeparator() + new Formatter()
        .format(leftAlignFormat, "Measure", "Product", "Time", "Value")
        .out()
        .toString + System.lineSeparator() + lines
    }

    val result = cells.map {
      case (cell, rowMembers, colMembers, yearMembers) =>
        val formatter = new Formatter()

        val row       = rowMembers.headOption.map(_.getUniqueName).getOrElse("<null>")
        val col       = colMembers.headOption.map(_.getUniqueName).getOrElse("<null>")
        val year      = yearMembers.headOption.map(_.getUniqueName).getOrElse("<null>")
        val cellValue = cell.getFormattedValue

        formatter.format(leftAlignFormat + "%n", col, row, year, cellValue).out().toString
    }.mkString

    println(s"""${Console.CYAN}Example Query${Console.RESET}:
         |$mdx
         |
         |${Console.CYAN}Result${Console.RESET}:
         |$titles
         |$result""".stripMargin)

    olapConnection.close()
  }
}
