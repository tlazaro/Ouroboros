package com.belfrygames.plat.player

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.belfrygames.utils._

trait Updateable {
  def update(elapsed : Long @@ Milliseconds) {

  }
  
  def keepUpdatable: Boolean = true
}

object Timed {
  val NS_IN_S = 1000000000L
  val NS_IN_MS = 1000000L
}

trait Timed { self: Updateable =>
  import Timed._

  private var nanos = 0L

  // Time in milliseconds
  var time = 0L

  final def reset() {
    nanos = 0L
    time = 0L
  }

  final def nanoUpdate(elapsedNanos : Long) {
    nanos += elapsedNanos

    val increment = nanos / NS_IN_MS

    time += increment
    nanos = nanos % NS_IN_MS

    update(tag[Milliseconds](increment))
  }
}

trait Particle {
  var x = 0.0f
  var y = 0.0f
  var z = 0.0f
}

trait Dynamic extends Particle {
  var xSpeed = 0.0f
  var ySpeed = 0.0f
}

trait Accelerated extends Dynamic {
  var xAccel = 0.0f
  var yAccel = 0.0f
}

trait DynamicUpdatable extends Updateable with Dynamic {
  abstract override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)

    x += elapsed * xSpeed
    y += elapsed * ySpeed
  }
}

trait AcceleratedUpdateable extends Updateable with Accelerated {
  abstract override def update(elapsed : Long @@ Milliseconds) {
    super.update(elapsed)

    xSpeed += elapsed * xAccel
    ySpeed += elapsed * yAccel

    x += elapsed * xSpeed
    y += elapsed * ySpeed
  }
}

trait Spatial {
  def width : Float
  def height : Float
}

trait Drawable {  
  var xOffset = 0
  var yOffset = 0
  var visible = true
  
  final def redraw (spriteBatch : SpriteBatch) {
    if (visible) {
      draw(spriteBatch)
    }
  }
  
  def keepDrawable: Boolean = true
  
  protected def draw(spriteBatch: SpriteBatch)
}

trait UpdateableParent extends Updateable {
  @volatile var updateables = Vector[Updateable]()
  
  final def addUpdateable[T<:Updateable](child : T): T = synchronized {
    updateables = updateables :+ child
    child
  }
  
  final def removeUpdateable[T<:Updateable](child : T) = synchronized {
    updateables indexOf child match {
      case n if n >= 0 => {
        val (prefix, suffix) = updateables splitAt n
        updateables = prefix ++ suffix.tail
      }
      case _ => error("Child does not exist:" + child)
    }
  }
  
  final def updateChildren(elapsed : Long @@ Milliseconds) = {
    updateables foreach (_ update elapsed)
    updateables filterNot (_.keepUpdatable) foreach removeUpdateable
  }
  
  override def update(elapsed : Long @@ Milliseconds) {
    updateChildren(elapsed)
  }
}

trait DrawableParent extends Drawable {
  @volatile var drawables = Vector[Drawable]()
  
  final def addDrawable[T<:Drawable](child : T): T = synchronized {
    drawables = drawables :+ child
    child
  }
  
  final def removeDrawable[T<:Drawable](child : T) = synchronized {
    drawables indexOf child match {
      case n if n >= 0 => {
        val (prefix, suffix) = drawables splitAt n
        drawables = prefix ++ suffix.tail
      }
      case _ => error("Child does not exist:" + child)
    }
  }
  
  final def drawChildren(spriteBatch: SpriteBatch) = {
    drawables foreach (_ redraw spriteBatch)
    drawables filterNot (_.keepDrawable) foreach removeDrawable
  }
}
