name := "LineageInitializer"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.0"

libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.0"

libraryDependencies += "za.co.absa.spline" % "spline-core" % "0.3.1"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "8.1.2.v20120308"

ivyXML :=
<dependency org="org.eclipse.jetty.orbit" name="javax.servlet" rev="3.0.0.v201112011016">
<artifact name="javax.servlet" type="orbit" ext="jar"/>
</dependency>


