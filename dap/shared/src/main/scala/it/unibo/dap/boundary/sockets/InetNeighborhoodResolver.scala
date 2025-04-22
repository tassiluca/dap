package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.NeighborhoodResolverComponent

/** A trait defining types for internet-based communications. */
trait InetTypes:
  /** The host address of a node in the network. */
  type Address = String
  /** The port number of a node in the network. */
  type Port = Int
  /** An endpoint, represented as a pair of address and port. */
  type Endpoint = (Address, Port)

/** A [[NeighborhoodResolverComponent]] that resolves distributed neighbors in an internet environment
  * with a reachable address (either a hostname or IP address) and a port number.
  */
trait InetNeighborhoodResolver extends NeighborhoodResolverComponent with InetTypes:
  override type Neighbor = Endpoint
