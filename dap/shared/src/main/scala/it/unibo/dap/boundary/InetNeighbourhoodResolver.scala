package it.unibo.dap.boundary

import it.unibo.dap.controller.NeighbourhoodResolverComponent

trait InetNeighbourhoodResolver extends NeighbourhoodResolverComponent:
  type Address = String
  type Port = Int
  type Endpoint = (Address, Port)
  override type Neighbour = Endpoint
