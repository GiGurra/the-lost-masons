package se.gigurra.thelostmasons

import java.util.UUID

import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import com.twitter.util.Await
import se.gigurra.fingdx.util.RestClient
import se.gigurra.serviceutils.twitter.logging.Logging

/**
  * Created by kjolh on 4/6/2016.
  */
object TheLostMasons extends Logging {

  val DEFAULT_HOST = "build.culvertsoft.se"
  val DEFAULT_PORT = "12340"
  val DEFAULT_INSTANCE = "thelostmasons"

  def main(args: Array[String]): Unit = {

    val playerConfig = getAppConfig(args)
    val lwjglConfig = getLwjglConfig(args)

    val client = RestClient(playerConfig.host, playerConfig.port, "keyboard input server")
    val app = App(playerConfig, client)

    // Check in.. see if the connection works
    Await.result(app.downloadPlayerInputs())

    // Now start it!
    new LwjglApplication(app, lwjglConfig)
  }

  def getAppConfig(args: Array[String]): AppConfig = {

    val argMap = args.zipWithIndex.map { case (a, b) => b -> a }.toMap

    val host = argMap.getOrElse(0, {
      logger.warning(s"No host argument, using default: $DEFAULT_HOST")
      DEFAULT_HOST
    })

    val port = argMap.getOrElse(1, {
      logger.warning(s"No name argument, using default: $DEFAULT_PORT")
      DEFAULT_PORT
    }).toInt

    val instance = argMap.getOrElse(2, {
      logger.warning(s"No instance argument, using default: $DEFAULT_INSTANCE")
      DEFAULT_INSTANCE
    })

    AppConfig( host, port, instance)

  }

  def getLwjglConfig(args: Array[String]): LwjglApplicationConfiguration = {
    new LwjglApplicationConfiguration {
      title = "The Lost Masons"
      forceExit = false
      vSyncEnabled = true
      width = 1280
      height = 720
      foregroundFPS = 60
      backgroundFPS = 60
      samples = 4
    }
  }

}



