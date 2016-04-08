package se.gigurra.thelostmasons

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.files.FileHandle

import scala.collection.mutable

object SoundPlayer {

  private val loadedEffects = new mutable.HashMap[String, Sound]
  private var playingMusic: Option[Music] = None

  private def loadFile(name: String) : FileHandle = {// Load the directory as a resource
    val dir_url = ClassLoader.getSystemResource(s"sounds/$name")
    val file = new java.io.File(dir_url.toURI)
    new FileHandle(file)
  }

  private def loadNewEffect(effectName: String): Sound = {
    val fileHandle = loadFile(effectName)
    Gdx.audio.newSound(fileHandle)
  }

  private def loadNewMusic(musicName: String): Music = {
    val fileHandle = loadFile(musicName)
    Gdx.audio.newMusic(fileHandle)
  }

  def playEffect(effectName: String, volume: Double = 1.0): Unit = {
    val effect = loadedEffects.getOrElseUpdate(effectName, loadNewEffect(effectName))
    effect.play(volume.toFloat)
  }

  def playMusic(track: String, volume: Double = 0.5): Unit = {
    playingMusic.foreach(_.stop())
    playingMusic.foreach(_.dispose())
    playingMusic = None
    playingMusic = Some(loadNewMusic(track))
    playingMusic.foreach(_.setVolume(volume.toFloat))
    playingMusic.foreach(_.setLooping(true))
    playingMusic.foreach(_.play())
  }

}
