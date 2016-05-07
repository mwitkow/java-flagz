package org.flagz

import scala.reflect.ClassTag

/** FlagField that supports Scala {@link Set}. */
class SetFlagField[E](defaultValue: Set[E])(implicit tag: ClassTag[E])
    extends ContainerFlagField[Set[E]](defaultValue, () => Set.empty[E]) {
  val elementClazz = tag.runtimeClass.asInstanceOf[Class[E]]

  override protected def addItem(existing: Set[E], value: String): Set[E] = {
    existing + ContainerFlagField.itemFromString(value, elementClazz, this)
  }

  override def valueString(value: Set[E]): String = {
    value.mkString(",")
  }
}

/** FlagField that supports Scala {@link List}. */
class ListFlagField[E](defaultValue: List[E])(implicit tag: ClassTag[E])
    extends ContainerFlagField[List[E]](defaultValue, () => Nil) {
  val elementClazz = tag.runtimeClass.asInstanceOf[Class[E]]

  override protected def addItem(existing: List[E], value: String): List[E] = {
    existing ::: List(ContainerFlagField.itemFromString(value, elementClazz, this))
  }

  override def valueString(value: List[E]): String = {
    value.mkString(",")
  }
}

/** FlagField that supports Scala {@link Map}. */
class MapFlagField[K, V](defaultValue: Map[K, V])(implicit keyTag: ClassTag[K], valueTag: ClassTag[V])
    extends ContainerFlagField[Map[K, V]](defaultValue, () => Map.empty[K, V]) {
  val keyClazz = keyTag.runtimeClass.asInstanceOf[Class[K]]
  val valueClazz = valueTag.runtimeClass.asInstanceOf[Class[V]]

  override protected def addItem(existing: Map[K, V], itemString: String): Map[K, V] = {
    val components: Array[String] = itemString.split(":")
    if (components.length != 2) {
      throw new FlagException.IllegalFormat(this, itemString, null)
    }
    val key: K = ContainerFlagField.itemFromString(components(0), keyClazz, this)
    val value: V = ContainerFlagField.itemFromString(components(1), valueClazz, this)
    existing + (key -> value)
  }

  override def valueString(value: Map[K, V]): String = {
    value.map { case (k, v) => (k.toString + ":" + v.toString) }.mkString(",")
  }
}