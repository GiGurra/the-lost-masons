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
                  var position: Vec2) extends Entity {

  val maxSpeed = 0.75

  def velocity: Vec2 = keys2Velocity(input.keysPressed) * maxSpeed

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
    out
  }

}
