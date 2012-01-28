package com.belfrygames.plat.player

import com.badlogic.gdx.math.Vector2
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
  def apply(): Int = math.max(((max * loop.fraction) - 1).toInt, 0)
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
    
    Boro.players find (p => (x >= p.x && x <= p.x + p.width && y >= p.y && y <= p.y + p.height)) match {
      case Some(p) => {
          Shot.removeShot(this)
          Boro.removePlayer(p)
        }
      case _ => if (game.level.collides(p)) {
          Shot.removeShot(this)
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
  val SPEED = 0.5f 
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
  
  val spitRightFrames = Art.spitRight.toList
  val spitLeftFrames = Art.spitLeft.toList
  val shootFunc = new CountFunc(tag(500), spitRightFrames.length)
  
  var frames = rightFrames
  var lookingRight = true
  
  var canFire = true
  var spitting = false
  var lastFrame = -1
  var cloning = false
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    if (spitting) {
      shootFunc update elapsed
      val nextFrame = shootFunc()
      if (lastFrame > nextFrame) {
        spitting = false
        lastFrame = -1
        frames = rightFrames
        
        var sprite = if (cloning) {
          val ball = new CloneShot(game)
          CloneShot.shots ::= ball
          game.followCam.target = ball
          ball
        } else {
          val ball = new Shot(game)
          Shot.shots ::= ball
          ball
        }
        
        val origin = north
        sprite.x = origin.x - sprite.width / 2
        sprite.y = origin.y - sprite.height / 2 - 40

        val dir = new Vector2(game.cursor.x - sprite.x, game.cursor.y - sprite.y)
        val speed = dir.nor.mul(Shot.SPEED)
        sprite.xSpeed = speed.x
        sprite.ySpeed = speed.y

        game.addUpdateable(sprite)
        game.regularCam.addDrawable(sprite)
        
        Boro.player.canFire = true
      } else {
        lastFrame = nextFrame
        textureRegion = frames(lastFrame)
        
        xSpeed = 0
        ySpeed = 0
        yAccel = 0
        return
      }
    }
    
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
  
  def shoot(cloning: Boolean) {
    this.cloning = cloning
    Boro.player.canFire = false
    frames = spitRightFrames
    spitting = true
  }
  
  var isAlive = true
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
  
  /**
   if (xSpeed == 0) {
   textureRegion = if (lookingRight) Art.right else Art.left
   } else {
   val p = if (xSpeed > 0) east else west
   val pX = game.level.globalToLocal((p.x + xSpeed).toInt, p.y.toInt)
   if (game.level.collides(pX)) {
   xSpeed = 0
   }
   }
    
   val (airLeft, airRight) = if (ySpeed > 0) { (topLeft, topRight) } else { (bottomLeft, bottomRight) }
   val p1 = game.level.globalToLocal((airLeft.x + xSpeed).toInt, (airLeft.y + ySpeed).toInt)
   val p2 = game.level.globalToLocal((airRight.x + xSpeed).toInt, (airRight.y + ySpeed).toInt)
   if (game.level.collides(p1) || game.level.collides(p2)) {
   val dir = if (ySpeed > 0) 1 else -1
      
   ySpeed = 0
   yAccel = 0
      
   var newY = p1.y
   do {
   newY += dir
   } while(newY >= 0 && newY < game.level.map.height &&
   (game.level.collides(Point2D[Int](p1.x, newY)) ||
   game.level.collides(Point2D[Int](p2.x, newY))))
      
   val dest = game.level.localToGlobal(p1.x, newY)
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
   */
}
