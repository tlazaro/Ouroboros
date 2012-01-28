package com.belfrygames.plat.player

import com.belfrygames.plat.Art
import com.belfrygames.plat.Ouroboros
import com.belfrygames.plat.utils.Loop
import com.belfrygames.plat.utils.Point2D
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

object Shot {
  val GRAVITY = -0.005f
  val SPEED = 2
  
  var shots = List[Shot]()
  def removeShot(s: Shot) {
    s.isAlive = false
    val (prefix, suffix) = shots splitAt shots.indexOf(s)
    shots = prefix ++ suffix.tail
  }
  
  def clear() {
    shots foreach (_.isAlive = false)
    shots = Nil
  }
}

class Shot(val game: Ouroboros) extends Sprite with AcceleratedUpdateable {
  textureRegion = Art.cursor
  yAccel = Shot.GRAVITY
  
  var isAlive = true
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    val xEdge = xSpeed + x + (if (xSpeed > 0) width else 0)
    val yEdge = ySpeed + y + (if (ySpeed > 0) height else 0)
    
    val p = game.level.globalToLocal(Point2D[Int](xEdge.toInt, yEdge.toInt))
    if (game.level.collides(p)) {
      Shot.removeShot(this)
    } else {
      Boro.players find (p => (x >= p.x && x <= p.x + p.width && y >= p.y && y <= p.y + p.height)) foreach { p =>
        Shot.removeShot(this)
        Boro.removePlayer(p)
      }
    }
  }
  
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
}

object CloneShot {
  val GRAVITY = -0.005f
  val SPEED = 2
  
  var shots = List[CloneShot]()
  def removeShot(s: CloneShot) {
    s.isAlive = false
    val (prefix, suffix) = shots splitAt shots.indexOf(s)
    shots = prefix ++ suffix.tail
  }
  
  def clear() {
    shots foreach (_.isAlive = false)
    shots = Nil
  }
}
 
class CloneShot(val game: Ouroboros) extends Sprite with AcceleratedUpdateable {
  textureRegion = Art.cursor
  yAccel = Shot.GRAVITY
  
  var isAlive = true
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    val xEdge = xSpeed + x + (if (xSpeed > 0) width else 0)
    val yEdge = ySpeed + y + (if (ySpeed > 0) height else 0)
    
    val p = game.level.globalToLocal(Point2D[Int](xEdge.toInt, yEdge.toInt))
    if (game.level.collides(p)) {
      val spawn = new Boro(game)
      spawn.x = x
      spawn.y = y
      
      CloneShot.removeShot(this)
      
      game addUpdateable spawn
      game.regularCam addDrawable spawn
      
      Boro.player.xSpeed = 0
      Boro.newPlayer(spawn)
      game.followCam.target = Boro.player
    }
  }
  
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
}

object Boro {
  val SPEED = 1.0f 
  val GRAVITY = -0.015f
  val JUMP_SPEED = 3f
  
  var player : Boro = _
  var players = List[Boro]()
  
  def newPlayer(player: Boro) {
    players ::= this.player
    this.player = player
  }
  
  def removePlayer(player: Boro) {
    player.isAlive = false
    val (prefix, suffix) = players splitAt players.indexOf(player)
    players = prefix ++ suffix.tail
  }
  
  def clear() {
    players foreach (_.isAlive = false)
    players = Nil
  }
}

class Boro(val game: Ouroboros) extends Sprite with AcceleratedUpdateable {
  textureRegion = Art.right
  
  yAccel = Boro.GRAVITY
  
  val rightFrames = Art.walkRight.toList
  val leftFrames = Art.walkLeft.toList
  val animfunc = new CountFunc(tag(1000), rightFrames.length)
  
  var frames = rightFrames
  var lookingRight = true
  
  var canFire = true
  
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    animfunc update elapsed
    
    import InputMappings._
    
    if (this == Boro.player) {
      if (left.isPressed) {
        xSpeed = -Boro.SPEED
      } else if (right.isPressed) {
        xSpeed = Boro.SPEED
      } else {
        xSpeed = 0
      }
    }
    
    if (y <= 0) {
      y = 0
      ySpeed = 0
    }
    
    if (this == Boro.player) {
      if (ySpeed == 0 && action.isPressed) {
        ySpeed = Boro.JUMP_SPEED
      }
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
    val pX = game.level.globalToLocal(Point2D[Int](nextX.toInt, y.toInt + textureRegion.getRegionHeight / 2))
    if (game.level.collides(pX)) {
      xSpeed = 0
    }
    
    nextX = x + xSpeed + textureRegion.getRegionWidth / 2
    var nextY = y + ySpeed + (if (ySpeed > 0) textureRegion.getRegionHeight else 0)
    val p = game.level.globalToLocal(Point2D[Int](nextX.toInt, nextY.toInt))
    if (game.level.collides(p)) {
      val dir = if (ySpeed > 0) 1 else -1
      
      ySpeed = 0
      yAccel = 0
      
      var newY = p.y
      do {
        newY += dir
      } while(newY >= 0 && newY < game.level.map.height && game.level.collides(Point2D[Int](p.x, newY)))
      
      val dest = game.level.localToGlobal(p.x, newY)
      y = dest.y
    } else {
      yAccel = Boro.GRAVITY
    }
    
    if (ySpeed != 0) {
      textureRegion = if (lookingRight) {
        Art.jumpRight(2)
      } else {
        Art.jumpLeft(2)
      }
    }
  }
  
  var isAlive = true
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
}
