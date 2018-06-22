package org.formcept.spline

import za.co.absa.spline.core.SparkLineageInitializer._
import org.apache.spark.sql.SparkSession

 class lineageInitializer extends java.io.Serializable {

  def initLineage(sparkSession: org.apache.spark.sql.SparkSession) ={
    sparkSession.enableLineageTracking()
  }
}

