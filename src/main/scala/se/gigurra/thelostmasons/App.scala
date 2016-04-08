package se.gigurra.thelostmasons

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Color
import com.twitter.finagle.FailedFastException
import com.twitter.util.{Duration, Future, NonFatal}
import se.gigurra.fingdx.util.{DefaultTimer, RestClient, Throttled}
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.fingdx.gfx.RenderContext._
import se.gigurra.fingdx.gfx.{GfxConfig, RenderCenter, World2DProjection}
import se.gigurra.fingdx.lmath.{Vec2, Vec3}

import scala.collection.mutable
import scala.util.Random

/**
  * Created by kjolh on 4/6/2016.
  */
case class App(config: AppConfig, keyboardServer: RestClient) extends ApplicationAdapter with Logging {

  val players = new mutable.HashMap[String, Player]

  override def create(): Unit = {
    DefaultTimer.fps(100) {
      downloadPlayerInputs()
    }
  }

  override def render(): Unit = frame {
    val dt = 1.0 / 60.0
    updatePlayers(dt)
    updateEnemies(dt)
    projection.viewport(viewportSize = cameraSize, offs = -cameraPos) {
      drawGround(dt)
      drawEnemies(dt)
      drawPlayers(dt)
    }
    projection.viewport(viewportSize = 2.0, offs = Vec2()) {
      drawGui(dt)
      drawTitle(dt)
    }
    removeAndAnnounceDeadStuff(dt)
  }

  ///////////////////////////////////////

  def downloadPlayerInputs(): Future[Unit] = {
    keyboardServer.get(s"${config.instance}", maxAge = Some(Duration.fromSeconds(5))).foreach { data =>
      networkInputSnapshots = Json.read[Map[String, DownloadedPlayerInput]](data)
    }.onFailure {
      case e: Throttled =>
      case e: FailedFastException =>
      case NonFatal(e) => logger.error(e, s"Failed to communicate with keyboard server")
    }.map(_ => ())
  }

  def announceNewPlayer(newPlayer: Player): Unit = {
    // Do something nice..
    logger.info(s"Player $newPlayer joined!")
  }

  def createNewPlayer(input: PlayerInput): Player = {
    new Player(
      name = input.userName,
      color = new Color(0.5f + Random.nextFloat() * 0.5f, 0.5f + Random.nextFloat() * 0.5f, 0.5f + Random.nextFloat() * 0.5f, 1.0f),
      input = input,
      position = Vec2()
    )
  }

  def updatePlayers(dt: Double) = {
    val inputs = networkInputSnapshots

    // Handle new players
    for ((name, input) <- inputs) {
      if (!players.contains(name)) {
        val newPlayer: Player = createNewPlayer(input.data)
        players.put(name, newPlayer)
        announceNewPlayer(newPlayer)
      }
    }

    // Set player inputs
    for ((name, input) <- inputs) {
      players.get(name).foreach {
        _.input = input.data
      }
    }

    // Do movements
    for ((name, player) <- players) {
      player.position += player.velocity * dt
    }

     // Fire weapons
    for ((name, player) <- players) {
      // ..
    }

    // Remove timed out players
    for ((name, player) <- players.toSeq.reverse) {
      if (!inputs.contains(name)) {
        players -= name
        announcePlayerLeft(player)
      }
    }

  }

  def updateEnemies(dt: Double) = {

    // Do random funky movements
    for ((name, player) <- players) {
      player.position += player.velocity * dt
    }

    // Fire weapons
    for ((name, player) <- players) {
      // ..
    }
  }


  def removeAndAnnounceDeadStuff(dt: Double) = {
  }

  ///////////////////////////////////////


  def announcePlayerLeft(player: Player) = {
    logger.info(s"Player $player left")
  }
  
  def drawGround(dt: Double) = {

  }

  def drawEnemies(dt: Double) = {

  }

  def drawPlayers(dt: Double): Unit = {
    for ((name, player) <- players) {
      at(player.position) {
        rect(0.1, 0.1, typ = FILL, color = player.color)
      }
    }
  }

  def drawGui(dt: Double) = {

  }

  def cameraPos: Vec2 = {
    val playerPositions = players.values.map(_.position)
    if (playerPositions.nonEmpty) {
      val minX = playerPositions.map(_.x).min
      val maxX = playerPositions.map(_.x).max
      val minY = playerPositions.map(_.y).min
      val maxY = playerPositions.map(_.y).max
      Vec3(0.5 * (minX + maxX), 0.5 * (minY + maxY), 0.0)
    } else {
      Vec3(0.0, 0.0, 0.0)
    }
  }

  def cameraSize: Double = {
    val playerPositions = players.values.map(_.position)
    if (playerPositions.nonEmpty) {
      val minX = playerPositions.map(_.x).min
      val maxX = playerPositions.map(_.x).max
      val minY = playerPositions.map(_.y).min
      val maxY = playerPositions.map(_.y).max
      math.max(2.0, math.max(maxX-minX,maxY-minY) * 1.25)
    } else {
      2.0
    }
  }

  def drawTitle(dt: Double) = {
    at((0.0, yTitle)) {
      s"The lost masons!".drawCentered(WHITE, scale = 3.0f)
    }
  }

  ///////////////////////////////////////

  val yTitle = 0.625

  implicit val projection = new World2DProjection(new RenderCenter {
    override def position: Vec3 = Vec3()
    override def heading: Double = 0.0
  })

  implicit val drawCfg = new GfxConfig {
    override def symbolScale: Double = 1.0
  }

  @volatile private var networkInputSnapshots = Map.empty[String, DownloadedPlayerInput]
}
