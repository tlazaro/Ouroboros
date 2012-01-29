package com.belfrygames.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL10
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.belfrygames.plat.player.UpdateableParent
import com.belfrygames.plat.player.DrawableParent
import com.belfrygames.plat.utils.Point2D
import com.belfrygames.plat.utils.StopWatch
import com.belfrygames.utils._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.belfrygames.plat.Art
import com.belfrygames.plat.camera.FollowCamera
import com.belfrygames.plat.player.Drawable
import com.belfrygames.plat.player.Timed
import scala.collection.mutable.ArrayBuffer

object Screen {
  var DEBUG = false
  var SHOW_KEYS = true
  
  sealed trait ResizePolicy
  case object FitScreen extends ResizePolicy
  case object Stretch extends ResizePolicy
  case object Original extends ResizePolicy
  
  var resizePolicy: ResizePolicy = FitScreen
}

class ScreenCam(val cam: OrthographicCamera) extends DrawableParent {
  override def draw (spriteBatch: SpriteBatch) {
    val m = spriteBatch.getProjectionMatrix.cpy
    spriteBatch.setProjectionMatrix(cam.combined)
    spriteBatch.begin()
    drawChildren(spriteBatch)
    spriteBatch.end()
    spriteBatch.setProjectionMatrix(m)
  }
}

class FakeCam extends ScreenCam(null) {
  override def draw (spriteBatch: SpriteBatch) {
    spriteBatch.begin()
    drawChildren(spriteBatch)
    spriteBatch.end()
  }
}

class Screen extends ApplicationListener with DrawableParent with UpdateableParent with Timed {
  import Screen._
  
  lazy val cam = new OrthographicCamera(Config.WIDTH, Config.HEIGHT)
  lazy val followCam = new FollowCamera(cam)
  
  lazy val regularCam = new ScreenCam(cam)
  lazy val hudCam = new ScreenCam(new OrthographicCamera(Config.WIDTH, Config.HEIGHT)) {
    cam.position.set(Config.WIDTH / 2, Config.HEIGHT / 2, 0)
    cam.update()
  }
  
  lazy val specialCam = new FakeCam
  
  protected[this] val renderables = new ArrayBuffer[Drawable]
  protected[this] val specialRenderables = new ArrayBuffer[Drawable]
  
  protected[this] lazy val spriteBatch  = new SpriteBatch()
  private[this] lazy val font = new BitmapFont()
  
  protected[this] val timer = new StopWatch
  val FPS = 60L
  
  val inputs = new InputMultiplexer()
  
  def create () {
    addUpdateable(followCam)
    
    cam.position.set(0, 0, 0)
    followCam.update(tag(0))
    hudCam.cam.position.set(0, 0, 0)
    
    addDrawable(specialCam)
    addDrawable(regularCam)
    addDrawable(hudCam)
    
    Gdx.input.setInputProcessor(inputs)
    inputs.addProcessor(DebugKeysController)
    
    Art.load()
    
    timer.start
  }
  
  final def render() {
    timer.end
    draw()
    nanoUpdate(timer.diff)
    timer.start
  }

  val tmp = new Vector3()
  val camDirection = new Vector3(1, 1, 0)
  val maxCamPosition = new Vector2(0, 0)
  
  override protected def draw(spriteBatch: SpriteBatch) {
    drawChildren(spriteBatch)
  }
  
  def screenToCamera(x: Int, y: Int) = {
    Point2D((x * Config.WIDTH / Gdx.graphics.getWidth) * cam.zoom, (y * Config.HEIGHT / Gdx.graphics.getHeight) * cam.zoom)
  }
  
  def draw() {
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
    
    val width = Gdx.graphics.getWidth
    val height = Gdx.graphics.getHeight
    val (tWidth, tHeight) = resizePolicy match {
      case Screen.FitScreen => if (width >= height) (height * Config.WIDTH / Config.HEIGHT, height) else (width, width * Config.WIDTH / Config.HEIGHT)
      case Screen.Stretch => (width, height)
      case Screen.Original => (Config.WIDTH, Config.HEIGHT)
    }
  
    Gdx.gl.glViewport((width - tWidth) / 2, (height - tHeight) / 2, tWidth, tHeight)
    
    draw(spriteBatch)
    
    Gdx.gl.glViewport(0,0, width, height)
    
    if (SHOW_KEYS || DEBUG) {
      spriteBatch.begin()
      if (SHOW_KEYS) {
        for ((text, line) <- keys) {
          font.draw(spriteBatch, text, 20, Gdx.graphics.getHeight - ((line + 1) * 20))
        }
      }
      if (DEBUG) {
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20)
        font.draw(spriteBatch, "Resize Policy: " + resizePolicy.getClass.getSimpleName, 20, 40)
        tmp.set(0, 0, 0)
        cam.unproject(tmp)
        font.draw(spriteBatch, "Location: " + cam.position.x + "," + cam.position.y, 20, 60)
      }
      spriteBatch.end()
    }
  }
  
  private[this] val keys = """Keys:
- Tab : Toggle Debug info
- F1 : Toggle this help
- Arrows or WASD: Move
- X or Space: Jump
- Left click: Normal shot
- Clone click: Clone shot
- Esc : Exit""".split("\n").view.zipWithIndex.toList
  
  def resume( ) { }
  def resize(width : Int, height : Int) { }
  def pause( ) { }
  def dispose( ) { }
}

object DebugKeysController extends InputAdapter {
  import com.badlogic.gdx.Input.Keys._

  override def keyUp(keycode : Int) : Boolean = {
    keycode match {
      case TAB => Screen.DEBUG = !Screen.DEBUG
      case F1 => Screen.SHOW_KEYS = !Screen.SHOW_KEYS
//      case F2 => Screen.resizePolicy = Screen.FitScreen
//      case F3 => Screen.resizePolicy = Screen.Original
//      case F4 => Screen.resizePolicy = Screen.Stretch
      case _ =>
    }

    false
  }
}