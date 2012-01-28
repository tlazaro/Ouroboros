package com.belfrygames.plat.player

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.belfrygames.plat.Art
import com.belfrygames.plat.utils.Loop
import com.belfrygames.tactics.InputMappings
import com.belfrygames.utils._

abstract class UpdateFunc[T](private[this] val lapse0 : Long @@ Milliseconds) extends Updateable {
  protected val loop = new Loop(lapse0)
    
  override def update(elapsed : Long @@ Milliseconds) {
    loop.update(elapsed)
  }
    
  def apply(): T
}
  
class CountFunc(private[this] val lapse0 : Long @@ Milliseconds,
                val max: Int) extends UpdateFunc[Int](lapse0) {
  def apply(): Int = ((max * loop.fraction) - 1).toInt
}
  
object Boro {
  val SPEED = 1.0f 
  val GRAVITY = -0.025f
  val JUMP_SPEED = 4f
}

class Boro extends Sprite with AcceleratedUpdateable {
  textureRegion = Art.right
  
  yAccel = Boro.GRAVITY
  
  
  val rightFrames = (for(col <- Art.walkRight) yield col(0)).take(8).toList
  val leftFrames = (for(col <- Art.walkLeft) yield col(0)).take(8).toList
  val animfunc = new CountFunc(tag(1000), rightFrames.length)
  
  var frames = rightFrames
  var lookingRight = true
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    animfunc update elapsed
    
    import InputMappings._
    
    if (left.isPressed) {
      xSpeed = -Boro.SPEED
    } else if (right.isPressed) {
      xSpeed = Boro.SPEED
    } else {
      xSpeed = 0
    }
    
    if (y <= 0) {
      y = 0
      ySpeed = 0
    }
    
    if (ySpeed == 0 && action.isPressed) {
      ySpeed = Boro.JUMP_SPEED
    }
    
    if (xSpeed > 0) {
      if (lookingRight) {
        frames = rightFrames
      }
      lookingRight = true
    } else if (xSpeed < 0) {
      if (!lookingRight) {
        frames = leftFrames
      }
      lookingRight = false
    }
    textureRegion = frames(animfunc())
    
    if (xSpeed == 0) {
      textureRegion = if (lookingRight) Art.right else Art.left
    }
  }
}
