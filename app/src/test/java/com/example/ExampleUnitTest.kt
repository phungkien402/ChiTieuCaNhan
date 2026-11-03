package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun currencyFormatting_isCorrect() {
    val formatted = com.example.util.CurrencyFormatter.formatVnd(150000.0)
    assertTrue(formatted.contains("150.000"))
  }

  @Test
  fun equalRoomSplitCalculation_isCorrect() {
    val totalAmount = 180000.0
    val totalPeople = 3
    val splitPerPerson = totalAmount / totalPeople
    assertEquals(60000.0, splitPerPerson, 0.001)
  }

  @Test
  fun memberDebtBreakdown_correctlyFiltersSplits() {
    val memberAId = "member_a"
    val memberBId = "member_b"
    val splits = listOf(
      com.example.model.RoomSplit(id = "1", title = "Đi chợ 1", totalAmount = 300000.0, participantIds = listOf(memberAId, memberBId), splitAmountPerPerson = 100000.0),
      com.example.model.RoomSplit(id = "2", title = "Mua gia vị", totalAmount = 60000.0, participantIds = listOf(memberAId), splitAmountPerPerson = 30000.0),
      com.example.model.RoomSplit(id = "3", title = "Ăn vặt", totalAmount = 80000.0, participantIds = listOf(memberBId), splitAmountPerPerson = 40000.0)
    )

    val memberASplits = splits.filter { it.participantIds.contains(memberAId) }
    assertEquals(2, memberASplits.size)
    assertEquals(130000.0, memberASplits.sumOf { it.splitAmountPerPerson }, 0.001)

    val memberBSplits = splits.filter { it.participantIds.contains(memberBId) }
    assertEquals(2, memberBSplits.size)
    assertEquals(140000.0, memberBSplits.sumOf { it.splitAmountPerPerson }, 0.001)
  }
}
