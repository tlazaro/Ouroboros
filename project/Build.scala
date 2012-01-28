import sbt._
import sbt.Keys._
import AndroidKeys._
import sbtassembly.Plugin._
import AssemblyKeys._
import ProguardPlugin._

object PlatformerBuild extends Build {
    val sharedSettings = Seq(
    )

    lazy val common = Project("common", file("common")) settings(sharedSettings :_*)
    lazy val resources = Project("resources", file("resources")) settings(sharedSettings :_*)

	lazy val android: Project = Project("android", file("android"),
          settings = AndroidGeneral.fullAndroidSettings ++ sharedSettings ++ Seq (
            proguardOption in Android := AndroidGeneral.options,
            mainAssetsPath in Android := file("resources/src/main/resources")        
          )
        ) dependsOn(common)

    lazy val desktop: Project = Project("desktop", file("desktop")) dependsOn(common, resources) settings(Seq(
        mainClass in Compile := Some("com.belfrygames.plat.Main"),
        mainClass in (Compile, run) := Some("com.belfrygames.plat.Main"),
        fork in run := true
    ) ++ assemblySettings ++ sharedSettings :_*)
}
 
object AndroidGeneral {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "Platformer Android",
    version := "0.1",
    platformName in Android := "android-10"
  )

  lazy val fullAndroidSettings =
    AndroidGeneral.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    )

  val options = """-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}"""
}

