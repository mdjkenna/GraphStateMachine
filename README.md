# Graph State Machine

### What is a GraphStateMachine and why use one ?
State machines provide a structured way to define states and transitions. 
They can enforce restrictions to prevent invalid or unintended state changes.
This is useful for situations that can be represented as a set of states with defined transitions between them.

State machines find uses in areas such as:
- Workflow automation
- GUI state management
- Event-driven systems
 
The `GraphStateMachine` represents the landscape of possible states within a state machine as a directed graph, where states are represented by vertices and transitions correspond to directed edges.
Using a directed graph as the foundation of a state machine allows for explicit enforcement of rules about valid state transitions while offering flexibility in defining complex relationships between states. 
The transitions and their conditions are encapsulated within the graph at creation time, ensuring that the state machine’s behavior _can be_ deterministic and easy to reason about (but not necessarily).

Depending on how it is used, a `GraphStateMachine` might behave in a similar way to a finite state machine (FSM).
The `GraphStateMachine` supports transition flags (`IEdgeTransitionFlags`) that can dynamically determine whether a transition is valid or not. 
When additional factors such as this are used to control transition logic, the state machine could be considered to be more complex than a traditional FSM.

In addition, a vertex is not prevented from being stateful itself, and could also potentially be used to control if an outgoing edge is traversed. 
Depending on how it is being used, the term `Extended Finite State Machine` might be a better fit when looking at additional state information to make transitions.

_In summary_: the `GraphStateMachine` is a state machine with possible states and transitions represented as a graph.  

Below is an example of a directed acyclic graph that can be represented in the library (diagram made using GraphViz):

<!--suppress CheckImageSize -->
<img src="ExampleEightVertexDAG.png" alt="Example Image" width="300"/>

### Creating a `GraphStateMachine`

You can create a `GraphStateMachine` object by using the `buildGraphStateMachine` function. This is an entrypoint to a series of DSL functions that allow various aspects of state machine behaviour to be configured.
Below is an example of how to create the directed graph in the GraphViz diagram above and use it is a model, using `buildGraphStateMachine` and `buildGraph` functions:

```kotlin
fun main() {
    val one = Vertex("1")
    val two = Vertex("2")
    val three = Vertex("3")
    val four = Vertex("4")
    val five = Vertex("5")
    val six = Vertex("6")
    val seven = Vertex("7")
    val eight = Vertex("8")
    
    val stateMachine = buildGraphStateMachine<Vertex> {
        buildGraph(one) {

            addVertex(one) {
                addOutgoingEdge {
                    setTo(two)
                }

                addOutgoingEdge {
                    setTo(three)
                }
            }

            addVertex(two) {
                addOutgoingEdge {
                    setTo(four)
                }
            }

            addVertex(three) {
                addOutgoingEdge {
                    setTo(five)
                }

                addOutgoingEdge {
                    setTo(six)
                }
            }

            addVertex(four) {
                addOutgoingEdge {
                    setTo(eight)
                }
            }

            addVertex(five) {
                addOutgoingEdge {
                    setTo(seven)
                }
            }

            addVertex(six) {
                addOutgoingEdge {
                    setTo(seven)
                }
            }

            addVertex(seven) {
                addOutgoingEdge {
                    setTo(eight)
                }
            }

            addVertex(eight) {}
        }
    }

}
```

#### Transition Flags and Handlers

The `GraphStateMachine` traverses the graph using Depth First Search (DFS).
Additional transition logic can be added using `IEdgeTransitionFlags`. 
Below is a snippet of building the graph shown above but with additional configuration added to 
the edge from vertex five to seven using the transition flags, which can block the state transition otherwise enabled by the edge.

The `setTransitionHandler` receives a `TransitionScope` that makes the flags available to the handler via the `flags` field, as well as
the vertex the edge is coming from via the `from` field.

```kotlin
buildGraphStateMachineWithTransitionFlags<Vertex, TransitionFlagsImpl> {
    setEdgeTransitionFlags(TransitionFlagsImpl())
    
    buildGraph(one) {
        // ... adding previous vertices
        addVertex(five) {
            addOutgoingEdge {
                setTo(seven)
                setTransitionHandler {
                    // flags available in TransitionScope can be used to block progression through that edge
                    !flags.blockedGoingTo7
                    // can also access the 'from' field here
                }
            }
        }
        // ... adding next vertices
    }
}   
```

#### Implementations of IVertex

The vertex that is added to the graph needs only to implement the `IVertex` interface.
This means it must provide a unique string identifier for the vertex. The identifier has to be unique within the graph.
Attempting to add duplicate identifiers when building the graph results in an error. 
From the perspective of graph traversal, the identifier (the `ìd` field on `IVertex`) is the only information that is used to identify a vertex.

There is already an implementation of `IVertex` provided in the library: `Vertex`

This is a convenience but does not have to be used. Any valid `IVertex` implementation can be used as a graph vertex.

#### Adding outgoing edges

Edges are added to the graph as directed outgoing edges from a vertex.
The vertex an edge is coming from will already be added to the graph.
The `to` property of an edge is the identifier of the vertex that the edge is pointing to.

When building the graph, a vertex with the identifier referenced in the `to` property does not need to exist within the graph at the exact instant the `to` property is set.
This allows for edges to be added to the graph in any order, as long as the vertex referenced by the `to` property exists within the graph before the graph is built (before the end of the DSL scope is reached).
By the time the end of the graph builder scope has been reached, all vertices referenced by a `to` property should have been added to the graph, or an error is thrown.

#### Traversal Types
The `EdgeTraversalType` class is an enum used to determine how the state machine traverses the graph in certain situations.
It allows the caller to select their desired behaviour in certain corner cases when traversing the graph.

There are two situations affected by the traversal type currently:
- An edge is not traversed because the edge's transition handler blocked the transition,
  but it later becomes a traversal candidate while its source vertex is still active
- The graph contains cycles

If neither of the above situations are encountered, then there is currently no difference between the traversal types.
The available options are below:

The traversal type is set using `EdgeTraversalType`
- **RetrogradeAcyclic:** Rechecks non-visited edges of the current vertex to explore previously skipped edges.
- **ForwardAcyclic:** Resumes traversal from the last visited edge of the current vertex, ignoring earlier skipped edges.
- **ForwardCyclic:** Similar to `ForwardAcyclic`, but it does not ignore cycles. Note when transitioning to a gray vertex the edge index is reset to the start.
  This can cause the state machine to cycle indefinitely through states of a cyclic path. This can be desirable, however caution is needed.
  Generally transition flags would need to be used to stop this behaviour from the outset, or eventually, after a delay.

The traversal type can be set inside: `GraphStateMachineBuilderScope` via the `setTraversalType` function.

```kotlin
buildGraphStateMachineWithTransitionFlags<TestVertex, TransitionFlagsImpl> {
    setEdgeTransitionFlags(TransitionFlagsImpl())
    setTraversalType(EdgeTraversalType.RetrogradeAcyclic)
    //... building the graph
}   
```

If not set explicitly, the default is: `RetrogradeAcyclic`

### Using the `GraphStateMachine`

The `dispatch` function is used to send actions (`GraphStateMachineAction`) to the state machine, which cause state transitions.
The main action is `Next` for forward traversal. 
This action might be the only action used depending on the implementation and requirements.

There is also a `Previous` action which can be used to go to previous states. 
Note that moving to the previous state is unconditional as the `Previous` action simply moves to the state machine to the previous state that it was in. 
Arriving at the previous state will cause edges to be explored from the beginning again for the vertex of the new state.
This default behaviour can of course be modified using custom transition flags, if desirable.

You can see in the below example how the state machine is traversed using the `Next` action using depth first search.

```kotlin
repeat(8) {
    println("Current ID: ${stateMachine.progress.currentStep.id}")
    stateMachine.dispatch(GraphStateMachineAction.Next)
}

/* Output:

Current ID: 1
Current ID: 2
Current ID: 4
Current ID: 8
Current ID: 3
Current ID: 5
Current ID: 7
Current ID: 6

*/
```

The path taken is as a result of DFS and ordered edges in the graph. 
Outgoing edges are visited in the order they are added to a vertex by default, 
however the order can be set manually in the edge builder scope to be different to the order of appearance.
The first edge that is valid is the one that is explored at a given point.

#### Cyclic Graph Example and Traversal Types
Below is an example of a 15 vertex graph that contains cycles (which can be used by a `GraphStateMachine`). 
It is a more complex example than the previous 8 vertex graph and produces more complex behavior. 
The selection of traversal type also becomes more significant, as does the design of transition handlers to manage cycle exploration (if applicable).

<!--suppress CheckImageSize -->
<img src="Example15VertexCyclic.png" alt="Example Image" width="500"/>

If setting the traversal type to be: `ForwardCyclic` and starting at vertex 1, 
the state machine will encounter an infinite loop around a cycle starting at vertex 6, the path of which can be seen in the below example. 
There are of course other cycles in the graph, but if starting at vertex 1 and traversing unconditionally, this cycle will not be escaped. 

```kotlin
repeat(10) {
    println("Current ID: ${stateMachine.progress.currentStep.id}")
    stateMachine.dispatch(GraphStateMachineAction.Next)
}

/* Output:

Current ID: 1
Current ID: 2
Current ID: 4
Current ID: 6
Current ID: 3
Current ID: 2
Current ID: 4
Current ID: 6
Current ID: 3
Current ID: 2
 */
```

Moving through cycles in this way can be useful.
Again this loop can be prevented or broken after a delay using transition flags to force an alternative edge to be traversed.
Of course, if the loop shouldn't exist at all then it should be avoided in the graph's construction.
 
Let's look at the same graph but with the traversal type set to `RetrogradeAcyclic`, 
which is the default option (**note:** unconditional traversal means `ForwardAcyclic` produces the same path).

```kotlin
repeat(14) {
    println("Current ID: ${stateMachine.progress.currentStep.id}")
    stateMachine.dispatch(GraphStateMachineAction.Next)
}

/* Output:

Current ID: 1
Current ID: 2
Current ID: 4
Current ID: 6
Current ID: 3
Current ID: 7
Current ID: 8
Current ID: 9
Current ID: 11
Current ID: 12
Current ID: 14
Current ID: 5
Current ID: 5
Current ID: 5

 */
```

You can see above that since cycles are being skipped when using the `RetrogradeAcyclic` traversal type the graph state machine takes a different path through the graph. 
Note the end state of 5 is reached, with no more available states as a result of ignoring cycles (going to 8 from 5 here would cause a cycle).
The graph discussed in this section can be constructed using the graph state machine builder DSL in the below snippet:

```kotlin
buildGraphStateMachine<Vertex> {
    //... other state machine configuration

    buildGraph(v1) {

        addVertex(v1) {
            addOutgoingEdge {
                setTo(v2)
            }
            addOutgoingEdge {
                setTo(v3)
            }
        }

        addVertex(v2) {
            addOutgoingEdge {
                setTo(v4)
            }
        }

        addVertex(v3) {
            addOutgoingEdge {
                setTo(v2)
            }

            addOutgoingEdge {
                setTo(v7)
            }

            addOutgoingEdge {
                setTo(v5)
            }
        }

        addVertex(v4) {
            addOutgoingEdge {
                setTo(v6)
            }
        }

        addVertex(v5) {
            addOutgoingEdge {
                setTo(v8)
            }
        }

        addVertex(v6) {
            addOutgoingEdge {
                setTo(v3)
            }
            addOutgoingEdge {
                setTo(v8)
            }
        }

        addVertex(v7) {
            addOutgoingEdge {
                setTo(v8)
            }
        }

        addVertex(v8) {
            addOutgoingEdge {
                setTo(v9)
            }
            addOutgoingEdge {
                setTo(v10)
            }
        }

        addVertex(v9) {
            addOutgoingEdge {
                setTo(v11)
            }
        }

        addVertex(v10) {
            addOutgoingEdge {
                setTo(v11)
            }
        }

        addVertex(v11) {
            addOutgoingEdge {
                setTo(v12)
            }
            addOutgoingEdge {
                setTo(v13)
            }
        }

        addVertex(v12) {
            addOutgoingEdge {
                setTo(v14)
            }
        }

        addVertex(v13) {
            addOutgoingEdge {
                setTo(v14)
            }
        }

        addVertex(v14) {
            addOutgoingEdge {
                setTo(v5)
            }
            addOutgoingEdge {
                setTo(v15)
            }
        }

        addVertex(v15) {
            addOutgoingEdge {
                setTo(v2)
            }
        }
    }
}

```

#### Concurrency

The `GraphStateMachine` is not inherently thread-safe.
Use of the state machine should be confined to a single thread.
There are many options to achieve this. Currently none are imposed.