package se.gigurra.thelostmasons

import se.gigurra.fingdx.util.CurTime

case class PassiveTimer(interval: Double, _initTime: Double = 0.0) {
  private var tLast: Double = _initTime

  private def isReached: Boolean = CurTime.seconds - tLast >= interval

  def executeIfTime(f: => Unit): Unit = {
    if (isReached) {
      tLast = CurTime.seconds
      f
    }
  }
}
