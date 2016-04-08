package se.gigurra.thelostmasons

import java.util.UUID

import com.badlogic.gdx.graphics.Color
import se.gigurra.fingdx.lmath.{Box, Vec2}
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.collection.mutable

case class SearchGrid[T <: Entity](_box: Box, nx: Int = 20, ny: Int = 20) extends Logging {

  type Bag[K, V] = mutable.HashMap[K, V]

  val box = Box(_box.width * 1.00001, _box.height * 1.00001, _box.center)
  val allBags: Seq[Bag[String, T]] = (0 until nx * ny).map(_ => new Bag[String, T]())
  val bagLkup = new Bag[String, Set[Int]]()
  val dx = box.width / nx.toDouble
  val dy = box.height / ny.toDouble

  def add(t: T): Unit = {
    require(box.contains(t.position), s"Entity $t is outside the bounding box!")
    remove(t)

    val bagIndices = bagIndicesFor(t.boundingBox)
    val bags = bagIndices.map(allBags.apply)

    bags.foreach(_.put(t.id, t))
    bagLkup.put(t.id, bagIndices)
  }

  def remove(t: T): Unit = {
    remove(t.id)
  }

  def remove(id: String): Unit = {
    bagLkup.remove(id).foreach { bagIndices =>
      bagIndices.map(allBags.apply).foreach { bag =>
        bag.remove(id)
      }
    }
  }

  def size: Int = bagLkup.size

  def getItemsNear(boundingBox: Box): Map[String, T] = {
    (for {
      iBag <- bagIndicesFor(boundingBox)
      bag = allBags(iBag)
      (id, item) <- bag
    } yield {
      id -> item
    }).toMap
  }

  def clear(): Unit = {
    bagLkup.clear()
    allBags.foreach(_.clear())
  }

  private def bagIndicesFor(boundingBox: Box): Set[Int] = {

    val ill = xy2ibag(boundingBox.left, boundingBox.bottom)
    val iur = xy2ibag(boundingBox.right, boundingBox.top)

    val (ixll, iyll) = i2ixiy(ill)
    val (ixur, iyur) = i2ixiy(iur)

    (for {
      ix <- ixll to ixur
      iy <- iyll to iyur
    } yield {
      ixiy2i(ix, iy)
    }).toSet
  }

  private def xy2ibag(x: Double, y: Double): Int = {
    val ix = x2ix(x)
    val iy = y2iy(y)
    ixiy2i(ix, iy)
  }

  private def y2iy(_y: Double): Int = {
    val y = Utils.clamp(_box.left, _y, _box.right)
    ((y - bottom) / dy ).floor.round.toInt
  }

  private def x2ix(_x: Double): Int = {
    val x = Utils.clamp(_box.left, _x, _box.right)
    ((x - left) / dx ).floor.round.toInt
  }

  private def ixiy2i(ix: Int, iy: Int): Int = {
    iy * nx + ix
  }

  private def i2ixiy(i: Int): (Int, Int) = {
    val ix = i % nx
    val iy = i / nx
    (ix, iy)
  }

  private def left: Double = box.left
  private def bottom: Double = box.left
}

object SearchGrid {

  def mkPlayer(): Player = {
    val name = UUID.randomUUID.toString
    val position = Utils.randomVector * 2.0 * math.random
    Player(name, Color.WHITE, PlayerInput(name, Set.empty[Int]), position)
  }


  def main(args: Array[String]): Unit = {
    val n = 5000

    val grid = SearchGrid[Entity](Box(4, 4))
    val players = (0 until n).map(_ => mkPlayer())

    players.foreach(grid.add)

    require(grid.size == n)

    grid.remove(players.head)

    require(grid.size == n-1)

    require(grid.getItemsNear(Box(1,1)).size > 5)
    require(grid.getItemsNear(Box(1,1)).size < n)
    require(grid.getItemsNear(Box(4,4)).size == n-1)
    require(grid.getItemsNear(Box(4,4,Vec2(1.5,1.5))).size < n-1)

    println(s"Items in grid: ${grid.size}")
    println("OK!")

  }
}