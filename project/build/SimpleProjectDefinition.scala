import sbt._

class SimpleProjectDefinition(info: ProjectInfo) extends DefaultProject(info) {
	override def mainClass = Some("HelloWorld")
}