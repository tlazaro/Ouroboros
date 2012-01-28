package com.belfrygames.tactics.screens

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.belfrygames.plat.player.Particle
import com.belfrygames.plat.player.Sprite

class Panel(val cWidth: Int, val cHeight: Int, val sideSize: Int, private[this] val region0: TextureRegion) extends Sprite with Particle {
  textureRegion = region0
  
  val topleft = new TextureRegion(textureRegion, 0, 0, cWidth, cHeight)
  val topright = new TextureRegion(textureRegion, cWidth + sideSize, 0, cWidth, cHeight)
  val bottomleft = new TextureRegion(textureRegion, 0, cHeight + sideSize, cWidth, cHeight)
  val bottomright = new TextureRegion(textureRegion, cWidth + sideSize, cHeight + sideSize, cWidth, cHeight)
  
  val top = new TextureRegion(textureRegion, cWidth, 0, sideSize, cHeight)
  val bottom = new TextureRegion(textureRegion, cWidth, cHeight + sideSize, sideSize, cHeight)
  val left = new TextureRegion(textureRegion, 0, cHeight, cWidth, sideSize)
  val right = new TextureRegion(textureRegion, cWidth + sideSize, cHeight, cWidth, sideSize)
  
  val middle = new TextureRegion(textureRegion, cWidth, cHeight, sideSize, sideSize)
  
  var _width = 0f
  override def width = _width
  def width_=(value: Float) = _width = value
  
  var _height = 0f
  override def height = _height
  def height_=(value: Float) = _height = value
  
  override def draw(spriteBatch : SpriteBatch) {
    if (textureRegion != null) {
      // Draw center
      spriteBatch.draw(middle, x + xOffset + cWidth, y + yOffset + cHeight, width - cWidth * 2, height - cHeight * 2)
      
      // Draw corners
      spriteBatch.draw(topleft, x + xOffset, y + yOffset + height - cWidth, cWidth, cHeight)
      spriteBatch.draw(topright, x + xOffset + width - cWidth, y + yOffset + height - cHeight, cWidth, cHeight)
      spriteBatch.draw(bottomleft, x + xOffset, y + yOffset, cWidth, cHeight)
      spriteBatch.draw(bottomright, x + xOffset + width - cWidth, y + yOffset, cWidth, cHeight)
      
      // Sides
      spriteBatch.draw(bottom, x + xOffset + cWidth, y + yOffset, width - cWidth * 2, cHeight)
      spriteBatch.draw(top, x + xOffset + cWidth, y + yOffset + height - cHeight, width - cWidth * 2, cHeight)
      spriteBatch.draw(left, x + xOffset, y + yOffset + cWidth, cWidth, height - cHeight * 2)
      spriteBatch.draw(right, x + xOffset + width - cWidth, y + yOffset + cHeight, cWidth, height - cHeight * 2)
    }
  }
}
