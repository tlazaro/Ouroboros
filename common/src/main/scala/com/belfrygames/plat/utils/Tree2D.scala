package com.belfrygames.plat.utils

class Tree2D[T] {
  import scala.collection.mutable.Map

  var data = Map.empty[Int,Map[Int,T]]

  override def toString = "QuadTree: "+toList

  def toList(): List[((Int,Int),T)] = {
    var result: List[((Int,Int),T)] = Nil
    var yMap: Map[Int,T] = null
    for(x <- data.keys) {
      yMap = data(x)
      for(y <-yMap.keys) {
        result = ((x,y), yMap(y)) :: result
      }
    }
    result
  }

  def add(x: Int, y: Int, element: T): T = {
    var yMap = data.getOrElseUpdate(x, Map[Int,T](y -> element) )
    var returnedElement = yMap.getOrElseUpdate(y, element)
    data.update(x, yMap)
    returnedElement
  }

  def remove(x: Int, y: Int): Option[T] = {
    data.getOrElse(x, None ) match{
      case None => None
      case map: Map[_,_] => {
          var yMap = map.asInstanceOf[Map[Int,T]]
          var removedElement: Option[T] = yMap.remove(y)
          if(yMap.isEmpty)
            data.remove(x)
          else
            data.update(x, yMap)
          return removedElement

        }
      case error => throw new Exception(error+" returned no type expected")
    }
  }

  def remove(element: T): Option[T] = {
    var xIterator = data.keysIterator
    while(xIterator.hasNext) {
      var x = xIterator.next
      var yMap = data(x)
      var yIterator = yMap.keysIterator
      while(yIterator.hasNext) {
        var y = yIterator.next
        if( yMap(y)== element){
          var removedElement: Option[T] = yMap.remove(y)
          if(yMap.isEmpty)
            data.remove(x)
          else
            data.update(x, yMap)
          return removedElement
        }
      }
    }

    None
  }

  def apply(x: Int, y: Int): Option[T] = {
    data.get(x) match {
      case None => None
      case Some(map: Map[_,_]) =>  map.asInstanceOf[Map[Int,T]].get(y)
      case error => throw new Exception(error + " returned no type expected")
    }
  }

  def containsElementAt(x: Int, y: Int): Boolean = {
    apply(x,y).isDefined
  }

  def contains(element: T): Boolean = {
    data.values.find(_.values.find(_ == element).isDefined).isDefined
  }

  def range(radius: Double, x: Double, y: Double): List[T] = {
    var results: List[T] = Nil

    for (xKey <- data.keys;  if (x - radius <= xKey && xKey <= x + radius)) {
      for (yValue <- data(xKey); val yKey = yValue._1; if (y - radius <= yKey && yKey <= y + radius)) {
        val _x = x - xKey
        val _y = y - yKey
        if (_x * _x + _y * _y <= radius * radius) {
          results ::= yValue._2
        }
      }
    }

    results
  }

  def rangeWithIndexes(radius: Double, x: Double, y: Double): List[((Int,Int),T)] = {
    var results: List[((Int,Int),T)] = Nil

    for (xKey <- data.keys;  if (x - radius <= xKey && xKey <= x + radius)) {
      for (yValue <- data(xKey); val yKey = yValue._1; if (y - radius <= yKey && yKey <= y + radius)) {
        val _x = x - xKey
        val _y = y - yKey
        if (_x * _x + _y * _y <= radius * radius) {
          results ::= ((xKey, yKey), yValue._2)
        }
      }
    }

    results
  }
}
