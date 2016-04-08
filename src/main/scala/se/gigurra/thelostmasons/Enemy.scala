package se.gigurra.thelostmasons

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.Vec2

case class Enemy(maxVelocity: Double, color: Color, var position: Vec2) extends Entity {
  var velocity: Vec2 = Vec2()
  val velocityUpdater = PassiveTimer(1)

  private def randomVelocity: Vec2 = {
    Utils.randomVector * maxVelocity
  }

  def updateVelocity(playerPositions: Iterable[Vec2]): Vec2 = {
    velocityUpdater.executeIfTime { velocity = randomVelocity }
    velocity
  }
}
