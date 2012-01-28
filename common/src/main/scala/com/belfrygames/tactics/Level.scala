package com.belfrygames.tactics

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.tiled.SimpleTileAtlas
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer
import com.badlogic.gdx.graphics.g2d.tiled.TiledLayer
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader
import com.badlogic.gdx.math.Vector3
import com.belfrygames.plat.player.Drawable
import com.belfrygames.plat.player.Updateable
import com.belfrygames.plat.utils.Point2D
import com.belfrygames.utils._
import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer

object Level {
  val COLLISION = "collision"
  val MARKERS = "markers"
  val PLAYER_START = 0
}

class Level (private[this] val file0: FileHandle, val camera : OrthographicCamera) extends Drawable with Updateable {
  val map = TiledLoader.createMap(file0)
  lazy val tileMapRenderer = new TileMapRenderer(map, new SimpleTileAtlas(map, file0.parent), 64, 64)
  val tmp = new Vector3()
  
  init()
  
  var start: Point2D[Int] = _
  
  private def init() {
    val markers = map.layers.filter(_.name == Level.MARKERS)
    map.layers.removeAll(markers)
    
    val collisionLayer = map.layers.filter(_.name == Level.COLLISION)
    map.layers.removeAll(collisionLayer)
    for (tileSet <- map.tileSets.toList) {
      val gid = tileSet.firstgid
      tileSet.name match {
        case Level.MARKERS => {
            for(layer <- markers) {
              for(y <- 0 until layer.tiles.size; x <- 0 until layer.tiles(0).size) {
                val tile = layer.tiles(y)(x) - gid
                if (tile == Level.PLAYER_START) {
                  start = localToGlobal(Point2D(x, y))
                }
              }
            }
          }
        case Level.COLLISION => {
          }
        case _ =>
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
