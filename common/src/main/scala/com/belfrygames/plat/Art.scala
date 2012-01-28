package com.belfrygames.plat

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion

object Art {
  lazy val menu = load("res/gui.png", 58, 58)
  lazy val menuCursor = load("res/gui.png", 18, 18, 60, 1)
  lazy val font = new BitmapFont
  lazy val left = load("res/left.png")
  lazy val right = load("res/right.png")
  lazy val walkRight = split("res/walkRight.png", 64, 138, 1, 1, false, false)
  lazy val walkLeft = split("res/walkLeft.png", 68, 146, 1, 1, false, false)
  
  def load () {
    font; menu; menuCursor; left; right; walkRight; walkLeft
  }
        
  private def split (name: String, width: Int, height: Int, flipX: Boolean = false, flipY: Boolean = false) : Array[Array[TextureRegion]] = {
    val texture = new Texture(Gdx.files.internal(name))
    val xSlices = texture.getWidth() / width
    val ySlices = texture.getHeight() / height
    val res = Array.ofDim[TextureRegion](xSlices, ySlices)
    for (x <- 0 until xSlices; y <- 0 until ySlices) {
      res(x)(y) = new TextureRegion(texture, x * width, y * height, width, height)
      res(x)(y).flip(flipX, flipY)
    }
    res
  }
  
  private def split(name: String, width: Int, height: Int, margin: Int, spacing: Int, flipX: Boolean, flipY: Boolean) : Array[Array[TextureRegion]] = {
    def indexToPos(x: Int, y: Int) : Tuple2[Int, Int] = {
        (margin + (width + spacing) * x, margin + (height + spacing) * y)
    }
    
    val texture = new Texture(Gdx.files.internal(name))
    val xSlices = texture.getWidth() / width
    val ySlices = texture.getHeight() / height
    val res = Array.ofDim[TextureRegion](xSlices, ySlices)
    for (x <- 0 until xSlices; y <- 0 until ySlices) {
      val coords = indexToPos(x, y)
      res(x)(y) = new TextureRegion(texture, coords._1, coords._2, width, height)
      res(x)(y).flip(flipX, flipY)
    }
    
    res
  }

  def load(name : String, width: Int = -1, height: Int = -1, x: Int = 0, y: Int = 0) : TextureRegion = {
    val texture = new Texture(Gdx.files.internal(name))
    if (width < 0 || height < 0) {
      new TextureRegion(texture, x, y, texture.getWidth, texture.getHeight)
    } else {
      new TextureRegion(texture, x, y, width, height)
    }
  }
}
