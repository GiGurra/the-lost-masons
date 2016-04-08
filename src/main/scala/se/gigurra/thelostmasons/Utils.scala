package se.gigurra.thelostmasons

import se.gigurra.fingdx.lmath.Vec2

object Utils {
  def randomVector: Vec2 = Vec2(math.random - 0.5, math.random - 0.5).normalized

  def clamp(min: Vec2, current: Vec2, max: Vec2): Vec2 = {
    Vec2(
      clamp(min.x, current.x, max.x),
      clamp(min.y, current.y, max.y)
    )
  }

  def clamp(min: Double, current: Double, max: Double): Double = {
    math.min(math.max(current, min), max)
  }
}
