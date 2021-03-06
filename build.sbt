val thelostmasons = Project(id = "thelostmasons", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
    mainClass in assembly := Some("se.gigurra.thelostmasons.TheLostMasons")
  )
  .dependsOn(uri("git://github.com/GiGurra/fin-gdx.git#0.1.0"))
