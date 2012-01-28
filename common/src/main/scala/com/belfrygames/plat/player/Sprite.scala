package com.belfrygames.plat.player

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

trait Sprite extends Drawable with Spatial {
  self: Particle =>

  var textureRegion : TextureRegion = null
  
  def width = textureRegion.getRegionWidth.toFloat
  def height = textureRegion.getRegionHeight.toFloat
  
  override def draw(spriteBatch : SpriteBatch) {
    if (textureRegion != null)
      spriteBatch.draw(textureRegion, x + xOffset, y + yOffset, width, height)
  }
}
