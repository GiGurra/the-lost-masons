package se.gigurra.thelostmasons

/**
  * Created by kjolh on 4/6/2016.
  */
object Json {

  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization._
  implicit val formats = Serialization.formats(NoTypeHints)

  def write[T <: AnyRef](x: T): String = {
    Serialization.write(x)
  }

  def read[T <: AnyRef: Manifest](source: String): T = {
    Serialization.read[T](source)
  }

}
