package dap.modelling

import dap.modelling.CTMC.*
import dap.utils.{ Grids, MSet }

/** Modules defining the concept of Distributed Asynchronous stochastic Petri net. */
object DAP:
  /** Rule of the net: `pre --rateExp--> eff | ^msg`. */
  case class Rule[P](pre: MSet[P], rateExp: MSet[P] => Double, eff: MSet[P], msg: MSet[P])

  /** Whole net's type. */
  type DAP[P] = Set[Rule[P]]

  /** A Token, localized in a given node, characterized by an ID. */
  case class Token[ID, P](id: ID, p: P)

  /** State of the network at a given time, with neighboring as a map. */
  case class State[ID, P](tokens: MSet[Token[ID, P]], messages: MSet[Token[ID, P]], neighbours: Map[ID, Set[ID]])

  // Local facility to extract the marking of a node.
  def localTokens[ID, P](tokens: MSet[Token[ID, P]], id: ID): MSet[P] =
    tokens.collect:
      case Token(`id`, t) => t

  // Here's the implementation of operational semantics
  def toPartialFunction[ID, P](spn: DAP[P]): PartialFunction[State[ID, P], Set[Action[State[ID, P]]]] =
    case State(tokens, messages, neighbours) =>
      ( // we first try to apply rules
        for
          Rule(pre, rateExp, eff, msg) <- spn // get any rule
          nodeId <- neighbours.keySet // get any node
          out <- tokens extract pre.map(Token(nodeId, _)) // checks if that node matches pre
          newtokens = out union eff.map(Token(nodeId, _)) // generate new tokens
          newmessages = messages union msg.map(Token(nodeId, _)) // generate new messages
          rate = rateExp(localTokens(tokens, nodeId)) // compute rate
        yield Action(rate, State(newtokens, newmessages, neighbours))
      ) ++ (
        for
          Token(id: ID, p: P) <- messages.asList.toSet // get any pending message
          newtokens = tokens union MSet.ofList(neighbours(id).toList.map(Token(_, p))) // compute spread tokens
          newmessages <- messages extract MSet(Token(id, p)) // drop the message
        yield Action(Double.PositiveInfinity, State(newtokens, newmessages, neighbours)) // note rate is infinity
      )

  def toCTMC[ID, P](spn: DAP[P]): CTMC[State[ID, P]] = CTMC.ofFunction(toPartialFunction(spn))

  def apply[P](rules: Rule[P]*): DAP[P] = rules.toSet

  def apply[P](rules: Set[Rule[P]]): DAP[P] = rules

end DAP

object DAPGrid:
  import DAP.*

  /** Prints a grid of counting of p's tokens in the form of tokens(messages). */
  def simpleGridStateToString[P](s: State[(Int, Int), P], p: P): String =
    Grids.gridLikeToString(
      s.neighbours.keySet.max._1,
      s.neighbours.keySet.max._1,
      (i, j) => localTokens(s.tokens, (i, j))(p).toString + "(" + localTokens(s.messages, (i, j))(p).toString + ")",
    )

  def gridStateToString[P](s: State[(Int, Int), P]): String =
    Grids.gridLikeToString(
      s.neighbours.keySet.max._1,
      s.neighbours.keySet.max._1,
      (i, j) =>
        s.tokens.collect {
          case Token((ii, jj), p) if ii == i && jj == j => p
        }.asList.mkString(", "),
    )
end DAPGrid
