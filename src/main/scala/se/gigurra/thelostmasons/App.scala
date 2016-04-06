package se.gigurra.thelostmasons

import com.badlogic.gdx.ApplicationAdapter
import com.twitter.finagle.FailedFastException
import com.twitter.util.{Duration, Future, NonFatal}
import se.gigurra.fingdx.util.{DefaultTimer, RestClient, Throttled}
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.fingdx.gfx.RenderContext._
import se.gigurra.fingdx.gfx.{GfxConfig, RenderCenter, World2DProjection}
import se.gigurra.fingdx.lmath.{Vec2, Vec3}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by kjolh on 4/6/2016.
  */
case class App(config: AppConfig, keyboardServer: RestClient) extends ApplicationAdapter with Logging {

  val players = new ArrayBuffer[Player]
  var started = false

  implicit val projection = new World2DProjection(new RenderCenter {
    override def position: Vec3 = Vec3()

    override def heading: Double = 0.0
  })

  implicit val drawCfg = new GfxConfig {
    override def symbolScale: Double = 1.0
  }

  override def create(): Unit = {
    DefaultTimer.fps(100) {
      downloadPlayerInputs()
    }
  }

  override def render(): Unit = frame {
    updatePlayers()
    updateEnemies()
    projection.viewport(viewportSize = cameraSize, offs = cameraPos) {
      drawGround()
      drawEnemies()
      drawPlayers()
    }
    projection.viewport(viewportSize = 2.0, offs = Vec2()) {
      drawGui()
      drawTitle()
    }
  }

  def drawGround() = {

  }

  def drawEnemies() = {

  }

  def drawPlayers(): Unit = {

  }

  def drawGui() = {

  }

  def downloadPlayerInputs(): Future[Unit] = {
    keyboardServer.get(s"${config.instance}", maxAge = Some(Duration.fromSeconds(5))).foreach { data =>
      println(data)
    }.onFailure {
      case e: Throttled =>
      case e: FailedFastException =>
      case NonFatal(e) => logger.error(e, s"Failed to communicate with keyboard server")
    }.map(_ => ())
  }

  def cameraPos: Vec2 = {
    val playerPositions = players.map(_.position)
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
    val playerPositions = players.map(_.position)
    if (playerPositions.nonEmpty) {
      val minX = playerPositions.map(_.x).min
      val maxX = playerPositions.map(_.x).max
      val minY = playerPositions.map(_.y).min
      val maxY = playerPositions.map(_.y).max
      math.max(2.0, math.max(maxX-minX,maxY-minY))
    } else {
      2.0
    }
  }

  def updatePlayers() = {
  }

  def updateEnemies() = {
  }

  def drawTitle() = {
    at((0.0, yTitle)) {
      s"The lost masons!".drawCentered(WHITE, scale = 3.0f)
    }
  }

  val yTitle = 0.625
  val yKeys = -0.1
  val keyWidth = 0.1
  val keyHeight = 0.1
  val keySpacing = math.max(keyWidth, keyHeight) * 1.5
  val arrowKeyCenter = Vec2(0.3, yKeys)
  val spacebarCenter = Vec2(-0.3, yKeys)
}
