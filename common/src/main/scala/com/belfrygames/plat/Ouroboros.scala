package com.belfrygames.plat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.belfrygames.core.Screen
import com.belfrygames.input.InputManager
import com.belfrygames.plat.player.Boro
import com.belfrygames.tactics.InputMappings
import com.belfrygames.tactics.Level
import com.belfrygames.utils._

class Ouroboros extends Screen {
  lazy val mapFile = Gdx.files.internal("res/prueba.tmx")
  lazy val level = new Level(mapFile, cam)
  
  lazy val boro = new Boro(level)
  override def create() {
    super.create()
    
    inputs.addProcessor(InputManager)
    
    import InputMappings._
    
    // Global mappings
    exit appendAction Gdx.app.exit
    
    addUpdateable(level)
    regularCam.addDrawable(level)
    
    specialCam.addDrawable(new com.belfrygames.plat.player.Drawable {
        override def draw (spriteBatch: SpriteBatch) {
          level.tileMapRenderer.render(cam)
        }
      })
    
    updateFollowCamRestrictions()
    
    addUpdateable(boro)
    regularCam.addDrawable(boro)

    maxCamPosition.set(level.tileMapRenderer.getMapWidthUnits(), level.tileMapRenderer.getMapHeightUnits())
    
    boro.x = level.start.x
    boro.y = level.start.y
  }
  
  override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)
  }
  
  def updateFollowCamRestrictions() {
    followCam.target = boro
    followCam.offset.x = level.map.tileWidth / 2
    followCam.offset.y = level.map.tileHeight / 2
    
    followCam.minX = cam.zoom * cam.viewportWidth / 2
    followCam.maxX = level.map.width * level.map.tileWidth - followCam.minX

    followCam.minY = cam.zoom * cam.viewportHeight / 2
    followCam.maxY = level.map.height * level.map.tileHeight - followCam.minY
  }
}
