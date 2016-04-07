package se.gigurra.thelostmasons

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.Vec2

/**
  * Created by kjolh on 4/6/2016.
  */
case class Player(name: String,
                  color: Color,
                  var input: PlayerInput,
                  var position: Vec2) {

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
