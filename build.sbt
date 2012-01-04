organization := "play.plugins"

name := "file-upload"

version := "1.0-SNAPSHOT"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
            "play"                             %%    "play"                     %   "2.0-RC1-SNAPSHOT",
            "commons-fileupload"                %    "commons-fileupload"       %   "1.2.2",
            "commons-io"                        %    "commons-io"               %   "2.1",
            "org.specs2"                        %%   "specs2"                   %   "1.6.1"    %   "test"
)
	

resolvers += "download.playframework.com" at "http://download.playframework.org/ivy-releases/"
  
