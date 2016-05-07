package org.flagz.testclasses

import org.flagz._

object TestObjectTwo extends FlagContainer {
  @FlagInfo(name = "test_two_int", help = "One's int")
  final val flagInt = ScalaFlagz.valueOf(300)

}

