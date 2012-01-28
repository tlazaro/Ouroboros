package com.belfrygames.plat.player

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.belfrygames.plat.utils.Point2D

trait Sprite extends Drawable with Spatial {
  self: Particle =>

  var textureRegion : TextureRegion = null
  
  def width = textureRegion.getRegionWidth.toFloat
  def height = textureRegion.getRegionHeight.toFloat
  
  override def draw(spriteBatch : SpriteBatch) {
    if (textureRegion != null)
      spriteBatch.draw(textureRegion, x + xOffset, y + yOffset, width, height)
  }
  
  def topLeft = Point2D(x, y + height)
  def topRight = Point2D(x + width, y + height)
  def bottomLeft = Point2D(x, y)
  def bottomRight = Point2D(x + width, y)
  
  def north = Point2D(x + width / 2, y + height)
  def south = Point2D(x + width / 2, y)
  def east = Point2D(x, y + height / 2)
  def west = Point2D(x + width, y + height / 2)
  
  def center = Point2D(x + width / 2, y + height / 2)
}
