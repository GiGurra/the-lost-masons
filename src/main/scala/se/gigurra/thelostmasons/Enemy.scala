package se.gigurra.thelostmasons

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.Vec2

case class Enemy(maxVelocity: Double, color: Color, scoreValue: Int, var position: Vec2) extends Entity {
  var velocity: Vec2 = Vec2()
  val velocityUpdater = PassiveTimer(0.2)

  private def randomVelocity: Vec2 = {
    Utils.randomVector * maxVelocity
  }

  def updateVelocity(playerPositions: Seq[Vec2]): Vec2 = {
    val lockedInVelocity = playerPositions
      .map(p => p - position)
      .filter(_.norm < 1)
      .sortBy(_.norm)
      .headOption
      .map(_.normalized * maxVelocity)

    velocityUpdater.executeIfTime { velocity = lockedInVelocity.getOrElse(randomVelocity) }
    velocity
  }
}
