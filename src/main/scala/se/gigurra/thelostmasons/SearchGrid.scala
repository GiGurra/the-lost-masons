package se.gigurra.thelostmasons

import se.gigurra.fingdx.lmath.Box
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
