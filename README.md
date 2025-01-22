![Build](https://github.com/mdjkenna/GraphStateMachine/actions/workflows/buildAndTest.yml/badge.svg)
[![codecov](https://codecov.io/gh/mdjkenna/GraphStateMachine/branch/master/graph/badge.svg)](https://codecov.io/gh/mdjkenna/GraphStateMachine)
[![](https://jitpack.io/v/mdjkenna/GraphStateMachine.svg)](https://jitpack.io/#mdjkenna/GraphStateMachine)

# Graph State Machine

### Adding the library to your project

Include the JitPack repository in your project. For example:

```kotlin
repositories {
  mavenCentral()
  maven("https://jitpack.io")
}
```

To add the library to your project, add the dependency to your `build.gradle.kts` file like below:

```kotlin
dependencies {
  implementation("com.github.mdjkenna:GraphStateMachine:<latest-release>")
}
```

You can refer to the latest release on JitPack at the top of the README.md file, or in the releases section of the repository.

### What is a graph state machine and why use one ?
State machines can provide a structured way to define possible states of a system,
including the sequences of states that a system is permitted to transition through.
They can enforce restrictions to prevent invalid or unintended state changes.

State machines find uses in areas such as:
- Workflow automation
- GUI state management
- Event-driven systems

The core concept of the `GraphStateMachine` is that it models states as vertices within a directed graph.
Enforcing valid transitions in the state machine can be done by verifying whether a path through the graph exists.
The `GraphStateMachine` represents the landscape of possible states within a state machine as a directed graph,
where states are represented by vertices.

Using a directed graph as the foundation of a state machine allows for:
- Explicit enforcement of rules about valid state transitions
- Supporting the encapsulation of state machine logic
- An easy to visualise model of possible states and transitions

Depending on usage, the `GraphStateMachine` may resemble a traditional finite state machine (FSM) or can act as an extended FSM by allowing transitions to be dynamically permitted or denied.

### Creating a `GraphStateMachine`

Below is a simplified Kotlin example illustrating how vertices and edges might be added:

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

  val stateMachine = buildGraphStateMachine<Vertex, String> {
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

      addVertex(eight)
    }
  }

}
```

The below image illustrates the structure of the graph in the example:

<!--suppress CheckImageSize -->
<img src="ExampleEightVertexDAG.png" alt="Example Image" width="200"/>

#### Implementations of IVertex

A vertex that is added to the graph needs to implement the `IVertex<I>` interface.
From the perspective of the state machine, the identifier for a vertex (the `id` field on `IVertex<I>`) is the only information that is used to identify a vertex.
The identifier has to be unique within the graph. Attempting to add duplicate identifiers when building the graph results in an error.

#### Adding edges

Edges are added by calling `addEdge` on the vertex scope within the builder.
Vertices can be referenced by edges before they are added to the graph.
All vertices referenced by edges must exist in the graph eventually within the builder function, or an error occurs.

#### Traversal Guards

Traversal guards can dynamically control edge traversal and are either open or closed.
Traversal guards are receiver functions that are set when building an edge,
returning `true` for open and `false` for closed.

They can dynamically reduce how many ways the graph can be traversed and consequently,
can dynamically reduce possible state transitions to a subset of those allowed within the graph's structure.

They are optional and don't need to be specified, in which case traversal is never prevented.

```kotlin
buildGraphStateMachine<Vertex, String, GuardState>(GuardState()) {
    
    buildGraph(one) {
        // ... adding previous vertices
        addVertex(five) {
            addOutgoingEdge {
                setTo(seven)
                setTransitionHandler {
                    // flags available in TransitionScope can be used to block progression through that edge
                    !guardState.blockedGoingTo7
                    // can also access the 'from' field here
                }
            }
        }
        // ... adding next vertices
    }
}   
```

**Note:** Specifying traversal guard state is optional.

### Using the `GraphStateMachine`

The `dispatch` function is used to send actions (`GraphStateMachineAction`) to the state machine, which cause state transitions.
**Note:** The `GraphStateMachine` moves through states _one at a time_, with one transition per action.

The main action is `Next` for forward traversal.
This action might be the only action used depending on the implementation and requirements.
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

The path taken is as a result of DFS and ordered edges in the graph, noting again there is one state transition per action.
Outgoing edges are visited in the order they are added to a vertex by default.

There is also a `Previous` action which can be used to go to previous states.

#### Cycles

Setting a `TraversalType` of `Cyclic` will enable cycles to be traversed.
Cycles are naturally supported by re-checking all edges whenever you move forward into a vertex, rather than permanently skipping any transition.
This means cycles create loops of where the first edge is re-traversed when a vertex is re-encountered in a cyclic path.
Where cycles occur, coordination with `TraversalGuard` functions is typically needed.
