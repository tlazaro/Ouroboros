package com.belfrygames.plat.player

import com.badlogic.gdx.math.Vector2
import com.belfrygames.plat.Art
import com.belfrygames.plat.Ouroboros
import com.belfrygames.plat.utils.Loop
import com.belfrygames.plat.utils.Point2D
import com.belfrygames.tactics.InputMappings
import com.belfrygames.utils._
import com.gemserk.commons.gdx.box2d.BodyBuilder
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType

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
      spawn.createBody
      
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
  val MAX_SPEED = 2.5f 
  val X_FORCE = 5.0f 
  val GRAVITY = -0.015f
  val JUMP_SPEED = 3f
  val EPSILON = 0.2f
  
  var player : Boro = _
  var players = List[Boro]()
  
  def newPlayer(player: Boro) {
    players ::= this.player
    this.player = player
  }
  
  def removePlayer(player: Boro) {
    player.isAlive = false
    player.game.box2d.destroyBody(player.body)
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
  
  var body: Body = _
  
  def createBody() {
    val builder = new BodyBuilder(game.box2d)
    val p  = Point2D[Float](game.screenToCam(x) + 1, game.screenToCam(y) + 1)
    body = builder.fixture(builder.fixtureDefBuilder.circleShape(1)).position(p.x, p.y).`type`(BodyType.DynamicBody).build
  }
  
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
      }
    }
    
    animfunc update elapsed
    
    import InputMappings._
    
    if (!spitting && this == Boro.player) {
      if (left.isPressed) {
        body.applyForceToCenter(-5, 0)
      } else if (right.isPressed) {
        body.applyForceToCenter(5, 0)
      }
      
      val v = body.getLinearVelocity
      if (math.abs(v.x) >= Boro.MAX_SPEED) {
        body.setLinearVelocity(if (v.x < 0) -Boro.MAX_SPEED else Boro.MAX_SPEED, v.y)
      }
      
      if (standing && action.isPressed) {
        val pos = body.getPosition
        body.applyLinearImpulse(0, 13f, pos.x, pos.y)
      }
    }
    
    x = game.camToScreen(body.getPosition.x) + 16
    y = game.camToScreen(body.getPosition.y) + 28
    
    if (!spitting) {
      val speed = body.getLinearVelocity
      speed.x match {
        case ab if ab > Boro.EPSILON =>  {
            frames = rightFrames
            lookingRight = true
          }
        case ab if ab < -Boro.EPSILON => {
            frames = leftFrames
            lookingRight = false
          }
        case _ =>
      }
      textureRegion = frames(animfunc())
    
      if (math.abs(speed.x) <= Boro.EPSILON) {
        textureRegion = if (lookingRight) Art.right else Art.left
      }
    
      import scala.collection.JavaConversions._
      game.box2d.getContactList.find(contact => contact.getFixtureA.getBody == body || contact.getFixtureB.getBody == body) match {
        case Some(contact) => {
            val other = if (contact.getFixtureA.getBody != body) contact.getFixtureA.getBody else contact.getFixtureB.getBody
          
            if (other.getPosition.y > body.getPosition.y) {
              textureRegion = if (lookingRight) Art.jumpRight(2) else Art.jumpLeft(2)
              standing = false
            } else {
              standing = true
            }
          }
        case _ => {
            textureRegion = if (lookingRight) Art.jumpRight(2) else Art.jumpLeft(2)
            standing = false
          }
      }
    }
  }
  
  var standing = true
  
  def shoot(cloning: Boolean) {
    this.cloning = cloning
    Boro.player.canFire = false
    frames = spitRightFrames
    spitting = true
  }
  
  var isAlive = true
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
}
