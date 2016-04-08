package se.gigurra.thelostmasons

import com.badlogic.gdx.{Input, ApplicationAdapter}
import com.badlogic.gdx.graphics.Color
import com.twitter.finagle.FailedFastException
import com.twitter.util.{Duration, Future, NonFatal}
import se.gigurra.fingdx.util.{DefaultTimer, RestClient, Throttled}
import se.gigurra.serviceutils.twitter.logging.Logging
import se.gigurra.fingdx.gfx.RenderContext._
import se.gigurra.fingdx.gfx.{ScreenProjection, GfxConfig, RenderCenter, World2DProjection}
import se.gigurra.fingdx.lmath.{Vec2, Vec3}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by kjolh on 4/6/2016.
  */
case class App(config: AppConfig, keyboardServer: RestClient) extends ApplicationAdapter with Logging {

  val entities = new mutable.HashMap[String, Entity]
  val deathList = new ArrayBuffer[String]()

  val worldSize = 4
  var enemySpawn = PassiveTimer(1)


  override def create(): Unit = {
    SoundPlayer.setPlayList(Seq("music1.mp3"))

    DefaultTimer.fps(100) {
      downloadPlayerInputs()
    }
  }

  override def render(): Unit = frame {
    val dt = 1.0 / 60.0
    updatePlayers(dt)
    updateEnemies(dt)
    updateBullets(dt)
    updateEntities(dt)
    clampToWorld()
    projection.viewport(viewportSize = cameraSize, offs = -cameraPos) {
      drawGround(dt)
      drawEnemies(dt)
      drawBullets(dt)
      drawPlayers(dt)
    }
    projection.viewport(viewportSize = 2.0, offs = Vec2()) {
      drawGui(dt)
      drawScore(dt)
    }
    handleDeath(dt)
  }

  ///////////////////////////////////////

  def downloadPlayerInputs(): Future[Unit] = {
    keyboardServer.get(s"${config.instance}", maxAge = Some(Duration.fromSeconds(5))).foreach { data =>
      networkInputSnapshots = Json.read[Map[String, DownloadedPlayerInput]](data)
    }.onFailure {
      case e: Throttled =>
      case e: FailedFastException =>
      case NonFatal(e) => logger.error(e, s"Failed to communicate with keyboard server")
    }.unit
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

  def updateEntities(dt: Double) = {
    import Utils.RichBoundingBox
    // Do movements
    for ((name, entity) <- entities) {
      entity.position += entity.velocity * dt
    }

    for {
      e1 <- entities.values
      e2 <- entities.values
      if !Utils.sameEntityType(e1, e2) && e1.id != e2.id && e1.boundingBox.overlap(e2.boundingBox)
    } {
      killEntity(e1)
      killEntity(e2)



      for {
        bulletHit <- Utils.asBulletOpt(e1)
        killer    <- bulletHit.parentPlayer
      } {
        killer.score += 1
      }
    }
  }

  def killEntity(entity: Entity) = deathList += entity.id

  def updatePlayers(dt: Double) = {
    val inputs = networkInputSnapshots

    // Handle new players
    for ((name, input) <- inputs) {
      if (!entities.contains(name)) {
        val newPlayer: Player = createNewPlayer(input.data)
        entities.put(name, newPlayer)
        announceNewPlayer(newPlayer)
      }
    }

    // Set player inputs
    for ((name, input) <- inputs) {
      entities.get(name).collect { case p: Player =>
        p.setInput(input.data)
      }
    }

    // Remove timed out players
    for (player <- players.toSeq.reverse) {
      if (!inputs.contains(player.id)) {
        killEntity(player)
        announcePlayerLeft(player)
      }
    }

  }

  def updateEnemies(dt: Double) = {
    import Utils.RichBoundingBox

    enemySpawn.executeIfTime {
      val enemy = new Enemy(0.7, RED, 1, Utils.randomVector)
      entities.put(enemy.id, enemy)
    }

    val playerPositions = players.map(_.position)
    for (enemy <- enemies) {
      enemy.updateVelocity(playerPositions.toSeq) * dt
    }
  }

  def updateBullets(dt: Double) = {
    for (player <- players) {
      if (player.input.keysPressed.contains(Input.Keys.SPACE)) {
        player.tryFire {
          SoundPlayer.playOneOf(Seq("shoot1.mp3", "shoot2.mp3", "laser.mp3"))
          val bullet = Bullet(player, 3, WHITE, player.direction * 2, player.position + player.direction * 0.15 )
          entities.put(bullet.id, bullet)
        }
      }
    }

    bullets.filter(b => b.maxDistance < b.traveledDistance).foreach(deathList += _.id)
  }

  def clampToWorld() = {
    playersAndEnemies.foreach { e =>
      e.position = Utils.clamp(lowerLeft, e.position, upperRight)
    }
  }

  def lowerLeft: Vec2 = -0.5 * Vec2(worldSize, worldSize)
  def upperRight: Vec2 = 0.5 * Vec2(worldSize, worldSize)

  def enemies: Iterable[Enemy] = entities.values.collect { case e: Enemy => e }
  def bullets: Iterable[Bullet] = entities.values.collect { case b: Bullet => b }
  def players: Iterable[Player] = entities.values.collect { case p: Player => p }
  def playersAndEnemies: Iterable[Entity] = players ++ enemies

  def handleDeath(dt: Double) = {
    deathList.distinct.foreach { id =>
      entities.get(id).foreach( _ match {
        case p: Player => SoundPlayer.playOneOf(Seq("end1.mp3", "end2.mp3", "wilhelm_scream.mp3"))
        case _ =>
      })
    }

    deathList.foreach(entities.remove(_))
    deathList.clear()
  }

  ///////////////////////////////////////


  def announcePlayerLeft(player: Player) = {
    logger.info(s"Player $player left")
  }

  def drawGround(dt: Double) = {
    val positions = -(worldSize - 1) to worldSize
    for(x <- positions) {
      for(y <- positions) {
        transform(_
          .translate(x = -0.25f, y = -0.25f)
          .scalexy(0.5)) {
          rect(width = 1, height = 1, at = Vec2(x, y), typ = LINE, DARK_GRAY)
        }
      }
    }
  }

  def drawEnemies(dt: Double) = {
    for (enemy <- enemies) {
      at(enemy.position) {
        rect(0.1, 0.1, typ = FILL, color = enemy.color)
      }
    }
  }

  def drawBullets(dt: Double) = {
    for (bullet <- bullets) {
      at(bullet.position) {
        circle(0.01,10, FILL, bullet.color)
      }
    }
  }

  def drawPlayers(dt: Double): Unit = {
    for (player <- players) {
      at(player.position) {
        rect(0.1, 0.1, typ = FILL, color = player.color)
      }
    }
  }

  def drawGui(dt: Double) = {

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
      math.max(2.0, math.max(maxX-minX,maxY-minY) * 1.25)
    } else {
      2.0
    }
  }

  def drawScore(dt: Double) = {
    at((-0.95, 0.95)) {
      val scoreBoard = players.map(p => s"${p.id.padRight(10).take(10)} ${p.score.padRight(4)}").mkString("\n")
      scoreBoard.drawRightOf(WHITE, scale = 2.0f)
    } (new ScreenProjection(
      new RenderCenter() {
        override def position: Vec3 = Vec3(0,0,0)
        override def heading: Double = 0
      }
    ))
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
