package se.gigurra.thelostmasons


import se.gigurra.fingdx.lmath.{Box, Vec2}

import scala.reflect.ClassTag
import scala.util.Random

object Utils {
  implicit class RichBoundingBox(box: Box) {
    def overlap(right: Box): Boolean = {
      right.contains(Vec2(box.left, box.bottom)) ||
        right.contains(Vec2(box.left, box.top)) ||
        right.contains(Vec2(box.right, box.bottom)) ||
        right.contains(Vec2(box.right, box.top)) ||
        box.contains(Vec2(right.left, right.bottom)) ||
        box.contains(Vec2(right.left, right.top)) ||
        box.contains(Vec2(right.right, right.bottom)) ||
        box.contains(Vec2(right.right, right.top))
    }
  }
  def randomVector: Vec2 = Vec2(math.random - 0.5, math.random - 0.5).normalized

  def clamp(min: Vec2, current: Vec2, max: Vec2): Vec2 = {
    Vec2(
      clamp(min.x, current.x, max.x),
      clamp(min.y, current.y, max.y)
    )
  }

  def sameEntityType(e1: Entity, e2: Entity): Boolean = {
    e1.getClass == e2.getClass
  }


  def clamp(min: Double, current: Double, max: Double): Double = {
    math.min(math.max(current, min), max)
  }

  def asBulletOpt(e: Entity): Option[Bullet] = {
    e match {
      case b: Bullet => Some(b)
      case _ => None
    }
  }

  def pickRandom[T](items: Seq[T]): T = {
    require(items.nonEmpty, "Cannot pick a random element from empty collection!")
    items.apply(Random.nextInt(items.size))
  }
}
