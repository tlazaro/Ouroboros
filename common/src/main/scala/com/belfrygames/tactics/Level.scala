package com.belfrygames.tactics

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import com.belfrygames.plat.player.Drawable
import com.belfrygames.plat.player.Updateable
import com.belfrygames.plat.utils.Point2D
import com.belfrygames.utils._
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.gemserk.commons.gdx.box2d.BodyBuilder
import scala.collection.JavaConversions._

object Level {
  val COLLISION = "collision"
  val MARKERS = "markers"
  val PLAYER_START = 0
  val PLAYER_END = 1
  
  val SOLID = 15
}

class Level (private[this] val file0: FileHandle, val camera : OrthographicCamera) extends Drawable with Updateable {
  val map = TiledLoader.createMap(file0)
  lazy val tileMapRenderer = new TileMapRenderer(map, new SimpleTileAtlas(map, file0.parent), 64, 64)
  val tmp = new Vector3()
  
  val collision = Array.ofDim[Int](map.width, map.height)
  var start: Point2D[Int] = _
  var end: Point2D[Int] = _
  
  init()
  private def init() {
    val markers = map.layers.filter(_.name == Level.MARKERS)
    map.layers.removeAll(markers)
    
    val collisionLayer = map.layers.filter(_.name == Level.COLLISION)
//    map.layers.removeAll(collisionLayer)
    
    for (tileSet <- map.tileSets.toList) {
      val gid = tileSet.firstgid
      tileSet.name match {
        case Level.MARKERS => {
            for(layer <- markers) {
              for(y <- 0 until layer.tiles.size; x <- 0 until layer.tiles(0).size) {
                val tile = layer.tiles(y)(x) - gid
                tile match {
                  case Level.PLAYER_START => start = localToGlobal(Point2D(x, y))
                  case Level.PLAYER_END => end = localToGlobal(Point2D(x, y))
                  case _ =>
                }
              }
            }
          }
        case "cave" => {
            for(layer <- collisionLayer) {
              for(y <- 0 until layer.tiles.size; x <- 0 until layer.tiles(0).size) {
                val tile = layer.tiles(y)(x) - gid
                if (0 <= tile && tile <= Level.SOLID) {
                  collision(x)(y) = 1
                }
              }
            }
          }
        case _ =>
      }
    }
  }
  
  def collides(p: Point2D[Int]): Boolean = {
    p match {
      case Point2D(x,y) if x < 0 || x >= collision.length || y < 0 || y >= collision(0).length => true
      case c => collision(c.x)(c.y) match {
        case 1 => true
        case _ => false
      }
    }
  }
  
  def createBox2D(box2d: World) {
    for(x <- 0 until collision.length; y <- 0 until collision(0).length) {
      if (collision(x)(y) == 1) {
        val builder = new BodyBuilder(box2d)
        val body = builder.fixture(builder.fixtureDefBuilder.boxShape(1, 1)).position(x * 2, 2 * (map.height - 1 - y)).`type`(BodyType.StaticBody).build
      }
    }
  }
  
  def localToGlobal(p: Point2D[Int]): Point2D[Int] = localToGlobal(p.x, p.y)
  def localToGlobal(x: Int, y: Int) = Point2D[Int](x * map.tileWidth, (map.height - 1 - y) * map.tileHeight)
  
  def globalToLocal(p: Point2D[Int]): Point2D[Int] = globalToLocal(p.x, p.y)
  def globalToLocal(x: Int, y: Int) = Point2D[Int](x / map.tileWidth, map.height - 1 - (y / map.tileHeight))

  override def update(elapsed: Long @@ Milliseconds) {
  }
  
  override def draw (spriteBatch: SpriteBatch) {
  }
}
