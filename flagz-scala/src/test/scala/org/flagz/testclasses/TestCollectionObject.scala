package org.flagz.testclasses

import org.flagz.{FlagContainer, ScalaFlagz, FlagInfo}

object TestCollectionObject extends FlagContainer {

  @FlagInfo(name = "test_coll_list", help = "For testing")
  final val flagList = ScalaFlagz.valueOf(List(300, 400, 500))

  @FlagInfo(name = "test_coll_map", help = "For testing")
  final val flagMap = ScalaFlagz.valueOf(Map("alpha" -> 0, "bravo" -> 1, "charlie" -> 2))

  @FlagInfo(name = "test_coll_set", help = "For testing")
  final val flagSet = ScalaFlagz.valueOf(Set("alpha", "bravo", "charlie"))
}
