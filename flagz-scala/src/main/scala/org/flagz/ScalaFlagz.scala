package org.flagz

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/**
 * Wrapper class containing utility methods for working with {@link Flag}
 * objects in Scala. Flag objects must be annotated with {@link FlagInfo} in order to
 * be recognized as a flag.
 *
 * The recommended way for definining flags in Scala is through objects. To make {@link Flag} objects discoverable
 * inside object definitions, the object must implement the {@link FlagContainer} trait. For example:
 *
  * ```
 * object MyObject extends FlagContainer {
 * {@literal @}FlagInfo(name = "num_threads", help = "Number of threads for request processing.")
 * final val threadCount = ScalaFlagz.valueOf(10)
 *
 * {@literal @}FlagInfo(name = "admin_users", help = "List of admin users.")
 * final val admins = ScalaFlagz.valueOf(List.empty[String])
 * }
  * ```
 *
 * There is built-in support for Scala immutable collections {@link Set}, {@link Map}, and {@link List}. Scala
 * primitives  (Int, Long, Double, Float, Short, Byte, String) are handled through built-in implicit conversions.
  **/
object ScalaFlagz extends Flagz {

  def valueOf(defaultValue: Int): Flag[java.lang.Integer] = {
    Flagz.valueOf(new java.lang.Integer(defaultValue))
  }

  def valueOf(defaultValue: Long): Flag[java.lang.Long] = {
    Flagz.valueOf(new java.lang.Long(defaultValue))
  }

  def valueOf(defaultValue: Float): Flag[java.lang.Float] = {
    Flagz.valueOf(new java.lang.Float(defaultValue))
  }

  def valueOf(defaultValue: Double): Flag[java.lang.Double] = {
    Flagz.valueOf(new java.lang.Double(defaultValue))
  }

  def valueOf(defaultValue: Short): Flag[java.lang.Short] = {
    Flagz.valueOf(new java.lang.Short(defaultValue))
  }

  def valueOf(defaultValue: Byte): Flag[java.lang.Byte] = {
    Flagz.valueOf(new java.lang.Byte(defaultValue))
  }

  def valueOf(defaultValue: Boolean): Flag[java.lang.Boolean] = {
    Flagz.valueOf(new java.lang.Boolean(defaultValue))
  }

  def valueOf(defaultValue: String): Flag[java.lang.String] = {
    Flagz.valueOf(new java.lang.String(defaultValue))
  }

  def valueOf[K, V](defaultValue: Map[K, V])(implicit keyTag: ClassTag[K], valueTag: ClassTag[V]): Flag[Map[K, V]] = {
    new MapFlagField[K, V](defaultValue)
  }

  def valueOf[E](defaultValue: List[E])(implicit tag: ClassTag[E]): Flag[List[E]] = {
    new ListFlagField[E](defaultValue)
  }

  def valueOf[E](defaultValue: Set[E])(implicit tag: ClassTag[E]): Flag[Set[E]] = {
    new SetFlagField[E](defaultValue)
  }

  /**
   * Parses the command line arguments and updates as necessary all {@link Flag}
   * objects annotated with {@link FlagInfo}.
   * <p>
   * If "--help" of "-h" is passed in at the command line, then the help menu
   * will be printed and the JVM will exit with a 0 exit status.
   *
   * @param args     command line arguments in the form
   *                 "--defaultFlagName=value --booleanFlag -c=foo ..."
   * @param packagePrefixes  list of Java packages to be scanned for Flag objects, keeping the scope narrow makes it
   *                         start fast. By default an empty list is used, meaning all classes will be reflected on.
   * @return registry useful for setting flags at runtime
   */
  def parse(args: Array[String], packagePrefixes: List[String]): FlagFieldRegistry = {
    Flagz.parse(args, packagePrefixes, ScalaObjectScanner.scanFlagObjects(packagePrefixes))
  }

  def parse(args: Array[String]): FlagFieldRegistry = {
    val emptyPrefixes = List.empty[String]
    Flagz.parse(args, emptyPrefixes, ScalaObjectScanner.scanFlagObjects(emptyPrefixes))
  }
}
