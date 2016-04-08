package se.gigurra.thelostmasons

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.Vec2

case class Bullet(parent: Entity, maxDistance: Double, color: Color, velocity: Vec2,  var position: Vec2) extends Entity {
  val initialPosition = position
  val scoreValue = 0

  def parentPlayer: Option[Player] = parent match {
    case p: Player => Some(p)
    case _         => None
  }

  def traveledDistance: Double = (position - initialPosition).norm
}
