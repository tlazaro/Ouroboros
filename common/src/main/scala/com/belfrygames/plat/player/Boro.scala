package com.belfrygames.plat.player

import com.belfrygames.plat.Art
import com.belfrygames.plat.utils.Loop
import com.belfrygames.plat.utils.Point2D
import com.belfrygames.tactics.InputMappings
import com.belfrygames.tactics.Level
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

object Shot {
  val GRAVITY = -0.005f
  val SPEED = 2
}
 
class Shot(val level: Level) extends Sprite with AcceleratedUpdateable {
  textureRegion = Art.cursor
  yAccel = Shot.GRAVITY
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
  }
}

object Boro {
  val SPEED = 1.0f 
  val GRAVITY = -0.015f
  val JUMP_SPEED = 3f
}

class Boro(val level: Level) extends Sprite with AcceleratedUpdateable {
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
    
    var nextX = x + xSpeed + (if (xSpeed > 0) textureRegion.getRegionWidth else 0)
    val pX = level.globalToLocal(Point2D[Int](nextX.toInt, y.toInt + textureRegion.getRegionHeight / 2))
    if (level.collides(pX)) {
      xSpeed = 0
    }
    
    nextX = x + xSpeed + textureRegion.getRegionWidth / 2
    var nextY = y + ySpeed + (if (ySpeed > 0) textureRegion.getRegionHeight else 0)
    val p = level.globalToLocal(Point2D[Int](nextX.toInt, nextY.toInt))
    if (level.collides(p)) {
      val dir = if (ySpeed > 0) 1 else -1
      
      ySpeed = 0
      yAccel = 0
      
      var newY = p.y
      do {
        newY += dir
      } while(level.collides(Point2D[Int](p.x, newY)))
      
      val dest = level.localToGlobal(p.x, newY)
      y = dest.y
    } else {
      yAccel = Boro.GRAVITY
    }
  }
}
