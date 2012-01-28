package com.belfrygames.plat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
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

class Ouroboros extends Screen {
  def levelName(n: Int) = "res/level" + n + ".tmx"
  def existsLevel(n: Int) = Gdx.files.internal(levelName(n)).exists
  
  def loadLevel(n: Int) = {
    level =  new Level(Gdx.files.internal(levelName(n)), cam)
    Boro.player.x = level.start.x
    Boro.player.y = level.start.y
    
    Boro.clear
    Shot.clear
    CloneShot.clear
    
    followCam.target = Boro.player
  }
  
  var level: Level = _
  var currentLevel = 0
  
  lazy val cursor = new Sprite with Particle with Updateable {
    textureRegion = Art.cursor
    
    override def update(elapsed : Long @@ Milliseconds) {
      x = cam.zoom * InputManager.x + cam.position.x - cam.zoom * Gdx.graphics.getWidth / 2 - width / 2
      y = cam.zoom * InputManager.y + cam.position.y - cam.zoom * Gdx.graphics.getHeight / 2- height / 2
    }
  }
  
  override def create() {
    super.create()
    
    Boro.player = new Boro(this)
    loadLevel(currentLevel)
    
    inputs.addProcessor(InputManager)
    
    cam.zoom = 2.0f
    
    import InputMappings._
    
    // Global mappings
    cloneShot.appendAction(() => {
        if (Boro.player.canFire) {
          Boro.player.canFire = false
          val ball = new CloneShot(this)
          CloneShot.shots ::= ball
          ball.x = Boro.player.x
          ball.y = Boro.player.y
          
          followCam.target = ball
      
          val dir = new Vector2(cursor.x - ball.x, cursor.y - ball.y)
          val speed = dir.nor.mul(Shot.SPEED)
          ball.xSpeed = speed.x
          ball.ySpeed = speed.y
        
          addUpdateable(ball)
          regularCam.addDrawable(ball)
        }
      })
    
    shot.appendAction(() => {
        if (Boro.player.canFire) {
          val ball = new Shot(this)
          Shot.shots ::= ball
          ball.x = Boro.player.x
          ball.y = Boro.player.y
          
          val dir = new Vector2(cursor.x - ball.x, cursor.y - ball.y)
          val speed = dir.nor.mul(Shot.SPEED)
          ball.xSpeed = speed.x
          ball.ySpeed = speed.y
        
          addUpdateable(ball)
          regularCam.addDrawable(ball)
        }
      })
    
    exit appendAction Gdx.app.exit
    
    addUpdateable(level)
    regularCam.addDrawable(level)
    
    specialCam.addDrawable(new com.belfrygames.plat.player.Drawable {
        override def draw (spriteBatch: SpriteBatch) {
          level.tileMapRenderer.render(cam)
        }
      })
    
    updateFollowCamRestrictions()
    
    addUpdateable(Boro.player)
    regularCam.addDrawable(Boro.player)
    
    addUpdateable(cursor)
    regularCam.addDrawable(cursor)

    maxCamPosition.set(level.tileMapRenderer.getMapWidthUnits(), level.tileMapRenderer.getMapHeightUnits())
  }
  
  
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
