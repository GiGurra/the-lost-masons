package se.gigurra.thelostmasons

import java.util.UUID

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.{Box, Vec2}

trait Entity {
  def velocity: Vec2
  var position: Vec2
  def color: Color
  def radius: Double = 0.05
  val id: String = UUID.randomUUID.toString
  def boundingBox: Box = Box(width = radius * 2, height = radius * 2, position)
}

/**
  * Created by kjolh on 4/6/2016.
  */
case class Player(name: String,
                  color: Color,
                  var input: PlayerInput,
                  var position: Vec2,
                  var score: Int = 0) extends Entity {

  override val id = name
  val maxSpeed = 0.75
  var lastFired = PassiveTimer(0.1)
  var direction = Vec2(1, 0)
  var velocity: Vec2 = Vec2(0, 0)

  def tryFire(f: => Unit) = lastFired.executeIfTime(f)

  def setInput(input: PlayerInput) = {
    this.input = input
    velocity = keys2Velocity(input.keysPressed)

    if (velocity.norm > 0.001) {
      direction = velocity.normalized
    }
  }

  def keys2Velocity(keys: Set[Int]): Vec2 = {
    import com.badlogic.gdx.Input.Keys._
    var out = Vec2()
    if (keys.contains(RIGHT))
      out += Vec2(1.0, 0.0)
    if (keys.contains(LEFT))
      out += Vec2(-1.0, 0.0)
    if (keys.contains(UP))
      out += Vec2(0.0, 1.0)
    if (keys.contains(DOWN))
      out += Vec2(0.0, -1.0)

    if (out.norm > 0.001)
      out = out.normalized

    out * maxSpeed
  }

}
