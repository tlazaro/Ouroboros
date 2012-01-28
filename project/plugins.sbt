resolvers += "nexus snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots"

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.7.2")

addSbtPlugin("org.scala-tools.sbt" % "sbt-android-plugin" % "0.6.1-SNAPSHOT")

resolvers += "cloudbees snapshots" at "https://repository-belfry.forge.cloudbees.com/snapshot"

credentials += {
    val credsFile = (Path.userHome / ".credentials")
    (if (credsFile.exists) Credentials(credsFile)
    else Credentials(file("/private/belfry/.credentials/.credentials")))
}

addSbtPlugin("xsbt-plugin-deployer" % "xsbt-plugin-deployer" % "0.1-SNAPSHOT")

//seq(netbeans.NetbeansTasks.netbeansSettings:_*)
