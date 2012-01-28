package com.belfrygames.plat

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication

class Main extends AndroidApplication {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    initialize(new com.belfrygames.plat.Ouroboros(), false)
  }
}
