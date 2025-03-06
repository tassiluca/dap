package it.unibo.dap.boundary

import it.unibo.dap.controller.NeighbourhoodResolverComponent

/** A [[NeighbourhoodResolverComponent]] that resolves distributed neighbours in an internet environment
  * with a reachable address (either a hostname or IP address) and a port number.
  */
trait InetNeighbourhoodResolver extends NeighbourhoodResolverComponent:
  type Address = String
  type Port = Int
  type Endpoint = (Address, Port)
  override type Neighbour = Endpoint
