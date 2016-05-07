package org

import java.util.function.{Predicate, Supplier}
import language.implicitConversions

package object flagz {

  /** Scala function to Java 8 {@link Supplier} implicit conversion. */
  implicit def FuncToJavaSupplier[T](func: () => T): Supplier[T] = {
    new Supplier[T] {
      override def get(): T = {
        func()
      }
    }
  }

  /** Java 8 {@link Supplier} to Scala function implicit conversion. Useful for getting {@link Flag} values. */
  implicit def JavaSupplierToFunc[T](supplier: Supplier[T]): (() => T) = {
    supplier.get
  }

  /** Scala function to Java 8 {@link Predicate} implicit conversion. */
  implicit def FuncToJavaPredicate[T](func: T => Boolean): Predicate[T] = {
    new Predicate[T] {
      override def test(value: T): Boolean = {
        func(value)
      }
    }
  }
}
