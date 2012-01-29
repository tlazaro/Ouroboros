package com.belfrygames.plat.player

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Contact
import com.belfrygames.plat.Art
import com.belfrygames.plat.Ouroboros
import com.belfrygames.plat.Sound
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
    s.game.box2d.destroyBody(s.body)
    val (prefix, suffix) = shots splitAt shots.indexOf(s)
    shots = prefix ++ suffix.tail
  }
  
  def clear() {
    shots foreach (_.isAlive = false)
    shots = Nil
  }
}

class PhysicObject(val game: Ouroboros) {
  self: Sprite with AcceleratedUpdateable =>
  var body: Body = _
  var contact: Contact = _
  
  def createBody() {
    val p  = Point2D[Float](game.screenToCam(x) + 1, game.screenToCam(y) + 1)
    createBody(p, 0)
  }
  
  def createBody(p: Point2D[Float], res: Float = 0.3f) {
    val builder = new BodyBuilder(game.box2d)
    body = builder.fixture(builder.fixtureDefBuilder.circleShape(1).restitution(res)).position(p.x, p.y).`type`(BodyType.DynamicBody).userData(this).build
    x = game.camToScreen(body.getPosition.x) + width / 2
    y = game.camToScreen(body.getPosition.y) + height / 2
  }
}

class Shot(private[this] val game0: Ouroboros) extends PhysicObject(game0) with Sprite with AcceleratedUpdateable {
  textureRegion = Art.saliva
  
  var isAlive = true
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    x = game.camToScreen(body.getPosition.x) + width / 2
    y = game.camToScreen(body.getPosition.y) + height / 2
    
    val speed = body.getLinearVelocity
    if (math.abs(speed.x) < Boro.EPSILON && math.abs(speed.y) < Boro.EPSILON) {
      Shot.removeShot(this)
    } else {
      Boro.players.find(p => math.abs(p.x - x) < 100 && math.abs(p.y - y) < 100) foreach {p =>
        p.kill()
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
    s.game.box2d.destroyBody(s.body)
    val (prefix, suffix) = shots splitAt shots.indexOf(s)
    shots = prefix ++ suffix.tail
  }
  
  def clear() {
    shots foreach (_.isAlive = false)
    shots = Nil
  }
}
 
class CloneShot(private[this] val game0: Ouroboros) extends PhysicObject(game0) with Sprite with AcceleratedUpdateable {
  textureRegion = Art.saliva
  
  var isAlive = true
  override def update(elapsed: Long @@ Milliseconds) {
    super.update(elapsed)
    
    if (contact != null) {
      val aBody = contact.getFixtureA.getBody
      val bBody = contact.getFixtureB.getBody
          
      Boro.players find (p => aBody == p.body || bBody == p.body) match {
        case None => {
            Sound.birth.play
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
        case _ =>
      }
    } else {
      x = game.camToScreen(body.getPosition.x) + width / 2
      y = game.camToScreen(body.getPosition.y) + height / 2
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
    val (prefix, suffix) = players splitAt players.indexOf(player)
    players = prefix ++ suffix.tail
  }
  
  def clear() {
    players foreach (_.isAlive = false)
    players = Nil
  }
}

class Boro(private[this] val game0: Ouroboros) extends PhysicObject(game0) with Sprite with AcceleratedUpdateable {
  textureRegion = Art.birth.head
  
  val rightFrames = Art.walkRight.toList
  val leftFrames = Art.walkLeft.toList
  val animfunc = new CountFunc(tag(1000), rightFrames.length)
  
  val spitRightFrames = Art.spitRight.toList
  val spitLeftFrames = Art.spitLeft.toList
  val shootFunc = new CountFunc(tag(500), spitRightFrames.length)
  
  val birthFrames = Art.birth.toList
  val birthFunc = new CountFunc(tag(500), birthFrames.length)
  
  val deathRightFrames = Art.deathRight.toList
  val deathLeftFrames = Art.deathLeft.toList
  val deathFunc = new CountFunc(tag(500), deathRightFrames.length)
  
  var frames = rightFrames
  var lookingRight = true
  
  var canFire = true
  var spitting = false
  var lastFrame = -1
  var cloning = false
  
  var birthing = true
  var dying = false
  
  def kill() {
    dying = true
    game.box2d.destroyBody(body)
  }
  
  override def update(elapsed: Long @@ Milliseconds) {
    if (!keepUpdatable) {
      return
    }
    
    super.update(elapsed)
    
    if (dying) {
      deathFunc update elapsed
      val nextFrame = deathFunc()
      if (lastFrame > nextFrame) {
        lastFrame = -1
        Boro.removePlayer(this)
      } else {
        lastFrame = nextFrame
        frames = if(lookingRight) deathRightFrames else deathLeftFrames
        textureRegion = frames(lastFrame)
      }
      
      x = game.camToScreen(body.getPosition.x) + 16
      y = game.camToScreen(body.getPosition.y) + 28
      
      return
    }
    
    if (birthing) {
      birthFunc update elapsed
      val nextFrame = birthFunc()
      if (lastFrame > nextFrame) {
        birthing = false
        lastFrame = -1
      } else {
        lastFrame = nextFrame
        textureRegion = birthFrames(lastFrame)
      }
      
      x = game.camToScreen(body.getPosition.x) + 16
      y = game.camToScreen(body.getPosition.y) + 28
    
      return
    }
    
    if (spitting) {
      shootFunc update elapsed
      val nextFrame = shootFunc()
      if (lastFrame > nextFrame) {
        spitting = false
        lastFrame = -1
        frames = rightFrames
        
        var sprite: PhysicObject with Sprite with AcceleratedUpdateable = if (cloning) {
          val ball = new CloneShot(game)
          CloneShot.shots ::= ball
          game.followCam.target = ball
          ball
        } else {
          val ball = new Shot(game)
          Shot.shots ::= ball
          ball
        }
        val pos = body.getPosition
        sprite.createBody(Point2D[Float](pos.x, pos.y + 1.2f))
        
        val dir = new Vector2(game.cursor.x - sprite.x, game.cursor.y - sprite.y)
        val speed = dir.nor.mul(20)
        sprite.body.setLinearVelocity(speed.x, speed.y)

        game.addUpdateable(sprite)
        game.regularCam.addDrawable(sprite)
        
        Boro.player.canFire = true
      } else {
        lastFrame = nextFrame
        textureRegion = frames(lastFrame)
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
        Sound.jump.play
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
    
      standing = true
      import scala.collection.JavaConversions._
      game.box2d.getContactList.find(contact => contact.getFixtureA.getBody == body || contact.getFixtureB.getBody == body) match {
        case Some(contact) => {
            val other = if (contact.getFixtureA.getBody != body) contact.getFixtureA.getBody else contact.getFixtureB.getBody
          
            if (math.abs(contact.getWorldManifold.getNormal.nor.x) <= Boro.EPSILON) {
              if (other.getPosition.y > body.getPosition.y) {
                textureRegion = if (lookingRight) Art.jumpRight(2) else Art.jumpLeft(2)
                standing = false
              }
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
    frames = if (game.cursor.x >= x) spitRightFrames else spitLeftFrames
    spitting = true
    Sound.shot.play
  }
  
  var isAlive = true
  override def keepUpdatable = isAlive
  override def keepDrawable = isAlive
}
