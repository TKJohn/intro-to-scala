package introcourse.level05

import introcourse.level05.ExceptionExercises.personStringPairs

// Map <- Functor
// flatMap <- Monad

// Option <- null
// Either <- Exception

// Either[SQLError, Option[User]] =>
// 1. Right(None) // Successful SQL query, but User does not exist
// 2. Right(user) // Successful SQL query. and User exists
// 3. Left(SQLError) // SQL query failed

/**
  * These exercises show the alternative to throwing Exceptions using the `Either` data type.
  * We will treat errors as values that our functions return.
  */
object EitherExercises {

  //ADT for representing errors as values
  sealed trait AppError

  case class EmptyName(message: String) extends AppError

  case class InvalidAgeValue(message: String) extends AppError

  case class InvalidAgeRange(message: String) extends AppError

  /**
    * In the ExceptionExercises exercise we used Exceptions to handle validation and
    * flow control. This is considered bad design as Exceptions are only for exceptional
    * situations!
    *
    * We also came across some issues when using this type of solution
    * with Scala.
    *
    * A summary of reasons why not to use Exceptions to model errors we can
    * recover from are:
    * 1. Creating a Stacktrace for an Exception is expensive
    * 2. Throwing Exceptions violates Referential Transparency
    * 3. Exceptions can't be composed (combined together)
    * 4. Nested try/catch blocks may lead to madness
    * 5. Scala doesn't warn us about the Exceptions a method may throw
    * 6. It's hard to treat Exceptions as values
    *
    * We now look at how to handle validations with Eithers which return success and errors
    * as values. This means that Eithers are referentially transparent. Eithers
    * use a Left value to denote an error and a Right value to denote a success
    * (what is Right is correct, what is not Right is wrong).
    *
    * sealed trait Either[+E, +A]
    * case class Right[A](value: A) extends Either[Nothing, A]
    * case class Left[E](error: E) extends Either[E, Nothing]
    */

  /**
    * Implement the function getName, so that it returns a Left with an EmptyName if the name supplied
    * is empty or a Right if the supplied name is not empty.
    *
    * scala> getName("Fred")
    * = Right(Fred)
    *
    * scala> getName("")
    * = Left(EmptyName(provided name is empty))
    **/
  def getName(providedName: String): Either[AppError, String] =
    if (providedName.nonEmpty) Right(providedName)
    else Left(EmptyName("provided name is empty"))

  /**
    * Implement the function getAge that returns a Left with an InvalidAgeValue if the age provided can't
    * be converted to an Int or a Left with a InvalidAgeRange if the provided age is not between 1 and 120
    * and returns a Right with an Int age if the age is valid.
    *
    * scala> getAge("20")
    * = Right(20)
    *
    * scala> getAge("Fred")
    * = Left(InvalidAgeValue(provided age is invalid: Fred))
    *
    * scala> getAge("-1")
    * = Left(InvalidAgeRange(provided age should be between 1-120: -1))
    *
    * Hint: use the toInt method to convert a String to an Int.
    */
  def getAge(providedAge: String): Either[AppError, Int] =
    providedAge.toIntOption match {
      case Some(value) =>
        if (1 <= value && value <= 120) Right(value)
        else Left(InvalidAgeRange(s"provided age should be between 1-120: $providedAge"))
      case None => Left(InvalidAgeValue(s"provided age is invalid: $providedAge"))
    }

  //
  //    try {
  //      val age = providedAge.toInt
  //      if (1 <= age && age <= 120) Right(age)
  //      else Left(InvalidAgeRange(s"provided age should be between 1-120: $providedAge"))
  //    } catch {
  //      case _: NumberFormatException => Left(InvalidAgeValue(s"provided age is invalid: $providedAge"))
  //    }

  /**
    * Implement the function createPerson, so that it returns a Right with a Person
    * if the name and age are valid or returns a Left of AppError if either the name or age is invalid.
    *
    * scala> createPerson("Fred", "32")
    * = Right(Person(Fred,32))
    *
    * scala> createPerson("", "32")
    * = Left(EmptyName(provided name is empty))
    *
    * scala> createPerson("Fred", "ThirtyTwo")
    * = Left(InvalidAgeValue(provided age is invalid: ThirtyTwo))
    *
    * scala> createPerson("Fred", "150")
    * = Left(InvalidAgeRange(provided age should be between 1-120: 150))
    *
    * Hint: Use a for-comprehension to sequence the Eithers from getName and getAge
    */
  def createPerson(name: String, age: String): Either[AppError, Person] =
    for {
      validName <- getName(name)
      validAge <- getAge(age)
    } yield Person(validName, validAge)

  /**
    * Reimplement createPerson using only `flatMap` and `map`
    */
  def createPerson2(name: String, age: String): Either[AppError, Person] =
    getName(name).flatMap(validName => getAge(age).map(validAge => Person(validName, validAge)))

  /**
    * scala> makeNameUpperCase("Fred", "32")
    * = Right(Person(FRED,32))
    *
    * scala> makeNameUpperCase("", "32")
    * = Left(EmptyName(provided name is empty))
    *
    * scala> makeNameUpperCase("Fred", "ThirtyTwo")
    * = Left(InvalidAgeValue(provided age is invalid: ThirtyTwo))
    *
    * scala> makeNameUpperCase("Fred", "150")
    * = Left(InvalidAgeRange(provided age should be between 1-120: 150))
    *
    * Hint: Use `createPerson` then use `map` and `copy`.
    *
    */
  def makeNameUpperCase(name: String, age: String): Either[AppError, Person] =
    createPerson(name, age).map(_.copy(name = name.toUpperCase))

  // createPerson(name, age).flatMap(person => createPerson(person.name.toUpperCase, age))

  /**
    * scala> createPersonAndShow("Fred", "32")
    * = "Fred is 32"
    *
    * scala> createPersonAndShow("", "32")
    * = "Empty name supplied"
    *
    * scala> createPersonAndShow("Fred", "ThirtyTwo")
    * = "Invalid age value supplied"
    *
    * scala> createPersonAndShow("Fred", "150")
    * = "Invalid age range supplied"
    *
    * Hint: Use `createPerson` then pattern match.
    *
    * You can pattern match on `Either` using its two constructors `Left` and `Right`:
    *
    * ```
    * eitherValue match {
    * case Left(error)  => // do something with error
    * case Right(value) => // do something with `value`
    * }
    * ```
    */
  def createPersonAndShow(name: String, age: String): String =
    createPerson(name, age) match {
      case Right(_) => s"$name is $age"
      case Left(_: EmptyName) => "Empty name supplied"
      case Left(InvalidAgeValue(_)) => "Invalid age value supplied"
      case Left(InvalidAgeRange(_)) => "Invalid age range supplied"
    }

  /**
    * Implement the function createValidPeople that uses the personStringPairs List
    * to create a List of Person instances.
    *
    * scala> createValidPeople
    * = List(Person(Tokyo, 30), Person(Berlin, 43))
    *
    * Hint: Use `map` and `collect`
    *
    */
  def createValidPeople: List[Person] =
    personStringPairs
      .map { case (name, age) => createPerson(name, age) }
      .collect { case Right(person) => person }

  /**
    * Implement the function collectErrors that collects all the errors
    * that occur while processing personStringPairs.
    *
    * scala> collectErrors
    * = List(InvalidAgeValue(provided age is invalid: 5o),
    * InvalidAgeRange(provided age should be between 1-120: 200),
    * InvalidAgeRange(provided age should be between 1-120: 0),
    * EmptyName(provided name is empty))
    *
    * Hint: Use `map` and `collect`
    */
  def collectErrors: List[AppError] =
    personStringPairs
      .map { case (name, age) => createPerson(name, age) }
      .collect { case Left(e) => e }
}
