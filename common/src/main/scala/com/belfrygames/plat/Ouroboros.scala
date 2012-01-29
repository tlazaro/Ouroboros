package com.belfrygames.plat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.belfrygames.core.Config
import com.belfrygames.core.Screen
import com.belfrygames.input.InputManager
import com.belfrygames.plat.player.Boro
import com.belfrygames.plat.player.CloneShot
import com.belfrygames.plat.player.Particle
import com.belfrygames.plat.player.Shot
import com.belfrygames.plat.player.Sprite
import com.belfrygames.plat.player.Updateable
import com.belfrygames.tactics.InputMappings
import com.belfrygames.tactics.Level
import com.belfrygames.utils._
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold

class Ouroboros extends Screen {
  def levelName(n: Int) = "res/level" + n + ".tmx"
  def existsLevel(n: Int) = Gdx.files.internal(levelName(n)).exists
  
  lazy val box2dCam = new OrthographicCamera(Config.WIDTH, Config.HEIGHT)
  
  val contactListener = new ContactListener() {
    def beginContact(contact: Contact) {
      
    }
    def endContact(contact: Contact) {
          
    }
    
    def postSolve(contact: Contact, impulse: ContactImpulse) {
      if (!contact.isTouching) return
          
      val aBody = contact.getFixtureA.getBody
      val bBody = contact.getFixtureB.getBody
      
      aBody.getUserData match {
        case p : Boro if p == Boro.player => 
        case p : Boro => bBody.getUserData match {
            case s: Shot => s.contact = contact
            case s: CloneShot => s.contact = contact
            case _ =>
          }
        case s: Shot => bBody.getUserData match {
            case p : Boro if p == Boro.player => 
            case p : Boro => s.contact = contact
            case _ => s.contact = contact
          }
        case s: CloneShot => bBody.getUserData match {
            case p : Boro if p == Boro.player => 
            case p : Boro => s.contact = contact
            case _ => s.contact = contact
          }
        case x => bBody.getUserData match {
            case s: Shot => s.contact = contact
            case s: CloneShot => s.contact = contact
            case _ =>
          }
      }
    }
    
    def preSolve(contact: Contact, oldManifold: Manifold) {
    }
  }
  
  def loadLevel(n: Int) = {
    if (box2d != null) {
      box2d.dispose
    }
    box2d = new World(gravity, false)
    
    box2d.setContactListener(contactListener)
    
    level =  new Level(Gdx.files.internal(levelName(n)), cam)
    Boro.player.x = level.start.x
    Boro.player.y = level.start.y
    
    Boro.clear
    Shot.clear
    CloneShot.clear
    
    followCam.target = Boro.player
    level.createBox2D(box2d)
    Boro.player.createBody
  }
  
  var level: Level = _
  var currentLevel = 1
  
  lazy val cursor = new Sprite with Particle with Updateable {
    textureRegion = Art.cursor
    
    override def update(elapsed : Long @@ Milliseconds) {
      x = cam.zoom * InputManager.x + cam.position.x - cam.zoom * Gdx.graphics.getWidth / 2 - width / 2
      y = cam.zoom * InputManager.y + cam.position.y - cam.zoom * Gdx.graphics.getHeight / 2- height / 2
    }
  }
  
  val gravity = new Vector2(0, -10)
  var box2d: World = _
  lazy val box2drenderer = new Box2DDebugRenderer
  
  override def create() {
    super.create()
    
    Sound.soundtrack.play
    Sound.soundtrack.setLooping(true)
    
    addUpdateable(new Updateable {
        override def update (elapsed: Long @@ Milliseconds) {
          box2d.step(0.1f, 3, 3)
        }
      })
    
    Boro.player = new Boro(this)
    loadLevel(currentLevel)
    
    inputs.addProcessor(InputManager)
    
    box2dCam.zoom = 64
    cam.zoom = 2.0f
    
    import InputMappings._
    
    // Global mappings
    cloneShot.appendAction(() => {
        if (Boro.player.canFire) {
          Boro.player.shoot(true)
        }
      })
    
    shot.appendAction(() => {
        if (Boro.player.canFire) {
          Boro.player.shoot(false)
        }
      })
    
    restart.appendAction(() => {
        loadLevel(currentLevel)
      })
    
    exit appendAction Gdx.app.exit
    
    addUpdateable(level)
    regularCam.addDrawable(level)
    
    specialCam.addDrawable(new com.belfrygames.plat.player.Drawable {
        override def draw (spriteBatch: SpriteBatch) {
          level.tileMapRenderer.render(cam)
          
          if (Screen.DEBUG) {
            val oldX = cam.position.x
            val oldY = cam.position.y

            val offset_x = screenToCam(oldX)
            val offset_y = screenToCam(oldY)

            cam.zoom = 2.0f / 32.0f
            cam.position.x = offset_x
            cam.position.y = offset_y
            cam.update

            box2drenderer.render(box2d, cam.combined)

            cam.zoom = 2.f
            cam.position.x = oldX
            cam.position.y = oldY
            cam.update
          }
        }
      })
    
    updateFollowCamRestrictions()
    
    addUpdateable(Boro.player)
    regularCam.addDrawable(Boro.player)
    
    addUpdateable(cursor)
    regularCam.addDrawable(cursor)

    maxCamPosition.set(level.tileMapRenderer.getMapWidthUnits(), level.tileMapRenderer.getMapHeightUnits())
  }
  
  def screenToCam(x: Float) = (x * cam.zoom / 64) - 1
  def camToScreen(x: Float) = (x - 1) * 64 / cam.zoom
  
  val dirToGoal = new Vector2(0.0f, 0.0f)
  val GOAL_DISTANCE = 64
  override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)
    
    dirToGoal.x = Boro.player.x - level.end.x
    dirToGoal.y = Boro.player.y - level.end.y
    
    if (Boro.players.isEmpty && dirToGoal.len2 <= GOAL_DISTANCE * GOAL_DISTANCE) {
      currentLevel += 1
      if (existsLevel(currentLevel))
        loadLevel(currentLevel)
      else
        Gdx.app.exit
    }
  }
  
  def updateFollowCamRestrictions() {
    followCam.offset.x = level.map.tileWidth / 2
    followCam.offset.y = level.map.tileHeight / 2
    
    followCam.minX = cam.zoom * cam.viewportWidth / 2
    followCam.maxX = level.map.width * level.map.tileWidth - followCam.minX

    followCam.minY = cam.zoom * cam.viewportHeight / 2
    followCam.maxY = level.map.height * level.map.tileHeight - followCam.minY
  }
}
