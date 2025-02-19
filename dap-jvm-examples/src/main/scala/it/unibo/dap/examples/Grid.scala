//package it.unibo.dap.examples
//
//import it.unibo.dap.modelling.DAP
//import it.unibo.dap.modelling.DAP.State
//import it.unibo.dap.utils.Grids
//
//object DAPGrid:
//  import DAP.*
//
//  /** Prints a grid of counting of p's tokens in the form of tokens(messages). */
//  def simpleGridStateToString[P](s: State[(Int, Int), P], p: P): String =
//    Grids.gridLikeToString(
//      s.neighbours.keySet.max._1,
//      s.neighbours.keySet.max._1,
//      (i, j) => localTokens(s.tokens, (i, j))(p).toString + "(" + localTokens(s.messages, (i, j))(p).toString + ")",
//    )
//
//  def gridStateToString[P](s: State[(Int, Int), P]): String =
//    Grids.gridLikeToString(
//      s.neighbours.keySet.max._1,
//      s.neighbours.keySet.max._1,
//      (i, j) =>
//        s.tokens.collect {
//          case Token((ii, jj), p) if ii == i && jj == j => p
//        }.asList.mkString(", "),
//    )
//end DAPGrid
