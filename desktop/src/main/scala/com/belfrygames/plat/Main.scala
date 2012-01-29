package com.belfrygames.plat

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.belfrygames.core.Config
import java.awt.Dimension
import java.awt.Toolkit

object Main {
  var FULLSCREEN = false
  
  def main(args: Array[String]): Unit = {
    if (args.length > 0 && args(0) == "-fullscreen") {
      FULLSCREEN = true
    }
    
    val config = new LwjglApplicationConfiguration
    config.useGL20 = true
    
    // Get the default toolkit
    val scrnsize = if (FULLSCREEN) {
      val toolkit = Toolkit.getDefaultToolkit()
      toolkit.getScreenSize()
    } else {
      new Dimension(Config.WIDTH, Config.HEIGHT)
    }
    
    config.width = scrnsize.width
    config.height = scrnsize.height
    
    config.title = "Ouroboros"
    config.fullscreen = FULLSCREEN
    config.useCPUSynch = false
    
    new LwjglApplication(new com.belfrygames.plat.Ouroboros(), config)
  }
}
