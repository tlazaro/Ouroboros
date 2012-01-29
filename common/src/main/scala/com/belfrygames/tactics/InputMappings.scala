package com.belfrygames.tactics

import com.badlogic.gdx.Input
import com.belfrygames.input.GameAction
import com.belfrygames.input.InputManager

object InputMappings {
  val up = new GameAction("up", GameAction.NORMAL)
  val down = new GameAction("down", GameAction.NORMAL)
  val left = new GameAction("left", GameAction.NORMAL)
  val right = new GameAction("right", GameAction.NORMAL)
  
  val menu = new GameAction("menu", GameAction.DETECT_INITAL_PRESS_ONLY)
  val action = new GameAction("action", GameAction.DETECT_INITAL_PRESS_ONLY)
  val cloneShot = new GameAction("clone", GameAction.DETECT_INITAL_PRESS_ONLY)
  val shot = new GameAction("shot", GameAction.DETECT_INITAL_PRESS_ONLY)
  
  val jump = new GameAction("jump", GameAction.NORMAL)
  
  val restart = new GameAction("restart", GameAction.DETECT_INITAL_PRESS_ONLY)
  
  val exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY)
  val pause = new GameAction("pause", GameAction.DETECT_INITAL_PRESS_ONLY)
  val zoomin = new GameAction("zoomin", GameAction.DETECT_INITAL_PRESS_ONLY)
  val zoomout = new GameAction("zoomout", GameAction.DETECT_INITAL_PRESS_ONLY)
  
  // All these mappings should also be configurable in runtime
  
  // System
  InputManager.mapToKey(exit, Input.Keys.ESCAPE)
  InputManager.mapToKey(restart, Input.Keys.R)
  InputManager.mapToKey(pause, Input.Keys.P)
  InputManager.mapToKey(zoomin, Input.Keys.PLUS)
  InputManager.mapToKey(zoomout, Input.Keys.MINUS)
  
  InputManager.mapToKey(jump, Input.Keys.V)

  // Special Actions
  InputManager.mapToKey(menu, Input.Keys.M)
  InputManager.mapToKey(menu, Input.Keys.ENTER)
  
  InputManager.mapToKey(shot, Input.Keys.Z)
  InputManager.mapToKey(action, Input.Keys.X)
  InputManager.mapToKey(cloneShot, Input.Keys.C)
  
  InputManager.mapToMouse(shot, Input.Buttons.LEFT)
  InputManager.mapToKey(action, Input.Keys.SPACE)
  InputManager.mapToMouse(cloneShot, Input.Buttons.RIGHT)
  
  // WASD
  InputManager.mapToKey(action, Input.Keys.W)
  InputManager.mapToKey(left, Input.Keys.A)
  InputManager.mapToKey(down, Input.Keys.S)
  InputManager.mapToKey(right, Input.Keys.D)
  
  // Arrows
  InputManager.mapToKey(action, Input.Keys.UP)
  InputManager.mapToKey(left, Input.Keys.LEFT)
  InputManager.mapToKey(down, Input.Keys.DOWN)
  InputManager.mapToKey(right, Input.Keys.RIGHT)
}
