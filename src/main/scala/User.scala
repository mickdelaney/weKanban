class User (val id: String, val firstName: String, val lastName: String) extends Entity {
	def fullName() = firstName + " " + lastName
}