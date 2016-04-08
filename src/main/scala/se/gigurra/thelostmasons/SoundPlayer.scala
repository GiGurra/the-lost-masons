package se.gigurra.thelostmasons

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music.OnCompletionListener
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.files.FileHandle
import scala.collection.mutable

object SoundPlayer {

  private val loadedEffects = new mutable.HashMap[String, Sound]
  private val loadedMusics = new mutable.HashMap[String, Music]
  private var currentPlaylist = Seq[Music]()

  private def loadFile(name: String) : FileHandle = {
    val fakeFile = new java.io.File(s"sounds/$name")
    new FileHandle(fakeFile) {
      override def read() = getClass.getClassLoader.getResourceAsStream(s"sounds/$name")
    }
  }

  private def loadNewEffect(effectName: String): Sound = {
    val fileHandle = loadFile(effectName)
    Gdx.audio.newSound(fileHandle)
  }

  private def loadNewMusic(musicName: String): Music = {
    val fileHandle = loadFile(musicName)
    Gdx.audio.newMusic(fileHandle)
  }

  private def loadSound(effectName: String): Sound = {
    loadedEffects.getOrElseUpdate(effectName, loadNewEffect(effectName))
  }

  private def loadMusic(musicName: String): Music = {
    loadedMusics.getOrElseUpdate(musicName, loadNewMusic(musicName))
  }

  def playEffect(effectName: String, volume: Double = 0.25): Unit = {
    loadSound(effectName).play(volume.toFloat)
  }

  def playOneOf(effectNames: Seq[String], volume: Double = 0.25): Unit = {
    if (effectNames.nonEmpty) {
      playEffect(Utils.pickRandom(effectNames), volume)
    }
  }

  def setPlayList(trackNames: Seq[String], shuffle: Boolean = false, volume: Double = 0.25): Unit = {

    // Stop prevous sounds
    currentPlaylist.foreach(_.stop())

    if (trackNames.nonEmpty) {

      currentPlaylist = trackNames.map(loadMusic)
      currentPlaylist.foreach(_.setVolume(volume.toFloat))
      currentPlaylist.zipWithIndex.foreach {
        case (item, i) =>
          item.setOnCompletionListener(new OnCompletionListener {
            override def onCompletion(music: Music): Unit = {
              if (shuffle) {
                Utils.pickRandom(currentPlaylist.toBuffer - music).play()
              } else {
                currentPlaylist((i + 1) % currentPlaylist.length).play()
              }
            }
          })
      }
      if (shuffle) {
        Utils.pickRandom(currentPlaylist).play()
      } else {
        currentPlaylist.head.play()
      }
    } else {
      currentPlaylist = Nil
    }
  }

}
