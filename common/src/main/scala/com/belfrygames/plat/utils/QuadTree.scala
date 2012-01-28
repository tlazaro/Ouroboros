package com.belfrygames.plat.utils

class QuadTree[T] {
  private var root : Node = null

  // helper node data type
  private class Node(val x : Float, val y : Float, val value : T) {
    var NW : Node = _
    var NE : Node = _
    var SE : Node = _
    var SW : Node = _
  }
    
  def insert(x : Float, y : Float, value : T) {
    root = insert(root, x, y, value);
  }

  private def insert(h : Node, x : Float, y : Float, value : T) : Node = {
    if (h == null) return new Node(x, y, value);
    //// if (eq(x, h.x) && eq(y, h.y)) h.value = value;  // duplicate
    else if ( less(x, h.x) && !less(y, h.y)) h.NW = insert(h.NW, x, y, value)
    else if (!less(x, h.x) && !less(y, h.y)) h.NE = insert(h.NE, x, y, value)
    else if ( less(x, h.x) &&  less(y, h.y)) h.SW = insert(h.SW, x, y, value)
    else if (!less(x, h.x) &&  less(y, h.y)) h.SE = insert(h.SE, x, y, value)
    return h
  }


  def query2D(x0 : Float, y0 : Float, x1 : Float, y1 : Float) : List[T] = {
    query2D(root, x0, y0, x1, y1)
  }

  private def query2D(h : Node, x0 : Float, y0 : Float, x1 : Float, y1 : Float) : List[T] = {
    var result : List[T] = Nil
        
    if (h != null) {
      if (x0 <= h.x && h.x <= x1 && y0 <= h.y && h.y <= y1)
        result ::= h.value

      if ( less(x0, h.x) && !less(y1, h.y)) result :::= query2D(h.NW, x0, y0, x1, y1)
      if (!less(x1, h.x) && !less(y1, h.y)) result :::= query2D(h.NE, x0, y0, x1, y1)
      if ( less(x0, h.x) &&  less(y0, h.y)) result :::= query2D(h.SW, x0, y0, x1, y1)
      if (!less(x1, h.x) &&  less(y0, h.y)) result :::= query2D(h.SE, x0, y0, x1, y1)
    }
        
    result
  }

  @inline private def less(k1 : Float, k2 : Float) = k1 < k2
  @inline private def eq  (k1 : Float, k2 : Float) = k1 == k2
}