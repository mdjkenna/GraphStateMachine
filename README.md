[![Build](https://github.com/mdjkenna/GraphStateMachine/actions/workflows/buildAndTest.yml/badge.svg)](https://github.com/mdjkenna/GraphStateMachine/actions/workflows/buildAndTest.yml)
[![codecov](https://codecov.io/gh/mdjkenna/GraphStateMachine/branch/master/graph/badge.svg)](https://codecov.io/gh/mdjkenna/GraphStateMachine)
[![](https://jitpack.io/v/mdjkenna/GraphStateMachine.svg)](https://jitpack.io/#mdjkenna/GraphStateMachine)
![GitHub](https://img.shields.io/github/license/mdjkenna/GraphStateMachine)
![GitHub last commit](https://img.shields.io/github/last-commit/mdjkenna/GraphStateMachine)

# GraphStateMachine

This is a Kotlin library for creating state machines that are designed by building a directed graph of states and transitions between them.
State machine behaviour is defined by specifying possible states and transitions in a Kotlin domain-specific-language (DSL) style builder.

Advantages of this approach include:

- **Validation**: The absence of an edge in the graph implicitly prevents invalid transitions

- **Declarative State Modeling**: The Kotlin DSL style builder helps you to avoid convoluted procedural constructs which can become difficult to maintain

- **Visualization and Communication**: Generate dot language representations your state machines to communicate and verify possible state transitions

- **Flexibility**: Combine the available features in this library to enable myriad ways of structuring state machines and tailoring their behaviour

- **Focus**: An implementation focused on doing one thing well without third party dependencies (except for coroutines), avoiding the addition of transitive dependencies to your project

Features and highlights:
- Configurable movement through the graph model
- Supports moving to previous states
- Cycles and conditional transitions through the graph are fully supported
- Effects are supported within the state model itself
- Action arguments can be dispatched to a state machine allowing for conditional transition decisions
- Predictable and atomic state transitions via a single-threaded actor model

There are two types of state machine in the library: `Traverser` and `Walker` - each can move through the graph differently, with its own set of advantages.
The API is explained in detail in the following sections.

## Adding the Library to Your Project

Include the JitPack repository in your project. For example:

```kotlin 
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

Then add GraphStateMachine as a dependency:

```kotlin
dependencies {
    implementation("com.github.mdjkenna:GraphStateMachine:<latest-release>")
}
```

Find the latest release version at the top of this README or in the Releases section of the GitHub repository.
All versions with a major version of `0` are experimental and may include breaking API changes.

## Getting Started

<details>
<summary>Defining a State Machine</summary>

The following 8 vertex directed acyclic graph can be represented easily in the graph builder DSL:

<!--suppress CheckImageSize -->
<img src="8VertexDAG.png" alt="Example Image" width="200"/>

_The above graph image was made using a dot language representation of the 8 vertex DAG in the example below and inputting this into GraphViz. 
Further customisation is available for these diagrams - discussed more in the last section._ 

GraphStateMachine provides a DSL for defining vertices (states) and edges (transitions) of your state machine graph. 
Vertices must implement the `IVertex<I>` interface, and edges define the allowed transitions between states.
Edges are traversed in the order they're added unless specified otherwise with an `order` parameter.

The following example creates a `Traverser` using the 8 vertex DAG in the image above using the graph builder DSL:

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

    val traverser = buildTraverser<Vertex, String> {
        buildGraph(one) {

            addVertex(one) {
                addEdge { // edges are traversed in order of appearance unless specified otherwise
                    setTo(two)
                }

                addEdge {
                    setTo(three)
                }
            }

            addVertex(two) {
                addEdge {
                    setTo(four)
                }
            }

            addVertex(three) {
                addEdge {
                    setTo(five)
                }

                addEdge {
                    setTo(six)
                }
            }

            addVertex(four) {
                addEdge {
                    setTo(eight)
                }
            }

            addVertex(five) {
                addEdge {
                    setTo(seven)
                }
            }

            addVertex(six) {
                addEdge {
                    setTo(seven)
                }
            }

            addVertex(seven) {
                addEdge {
                    setTo(eight)
                }
            }

            addVertex(eight)
        }
    }
}
```

In this example, edges are traversed using DFS, with neighbouring edges explored in the order they are added to a vertex. 
For vertex "one", the edge to "two" will be tried first, followed by the edge to "three". 
You can also explicitly set the traversal order using the `order` parameter in `addEdge`.

#### Implementations of IVertex

A vertex added to the graph must implement the `IVertex<I>` interface.
The vertex id must be unique within the graph. Adding duplicate ids when building the graph results in an error.

Any valid `IVertex<I>` implementation can be used as a graph vertex.
The `id` field is of type `I`.
The library provides predefined simple vertex implementations for convenience.
You can also use custom vertex implementations with user-defined types for `I`.

#### Adding outgoing edges

Add edges to the graph as directed outgoing edges _from_ a vertex.
Once the graph is built, edges have a fixed traversal order to ensure predictable and consistent edge visitation.

The vertex an edge is coming from will already be added to the graph.
The `to` property of an edge is the identifier of the vertex the edge points to.

All vertices referenced by a `to` property should be added before building the graph or an error is thrown.
However, an edge can temporarily reference a vertex that hasn't been added to the graph while the graph is being built.
This allows adding edges to the graph in any order, as long as the vertex referenced by the `to` property exists within the graph by the end of the builder function.

</details>

<details>
<summary>Walkers and Traversers: Use Cases</summary>

The most efficient and practical choice between a traverser or walker depends on the use case.

##### Traversers
Traversers implement standard depth-first search (DFS) which naturally includes backtracking,
meaning they will search back through the ancestor vertices of their current path to look for unvisited vertices.
When a traverser reaches a vertex with no valid outgoing edges, it will backtrack to find alternative paths.
They also support moving to previous states.

You can access the history of visited states on Traversers using the `tracePath()` method:

```kotlin
val path = traverser.tracePath()
```

This returns a list of vertices representing the traversal path, ordered from start to current.

###### Considerations if using a traverser

Traversers retain breadcrumbs to support their backtracking and bidirectional abilities.
As a result their memory usage is not constant but slowly increases the further they traverse.
This is only significant in specific scenarios.   
Note that moving to previous vertices does the opposite - removing breadcrumbs.
If traversing constantly in a long-running loop for example this could become a consideration

###### Use cases for Traversers

Traversers are naturally suited to scenarios where DFS traversal through a state model is desired i.e. backtracking.
For example: An application wizard or workflow, navigation through screens, a finite custom protocol for handling data validation.
The `tracePath()` method mentioned above is particularly useful for processing wizard or workflow results.

##### Walkers
Walkers just transition through the first valid edge that does not block them using a transition guard.
When a walker reaches a vertex with no valid outgoing edges it simply stops as it doesn't retain breadcrumbs for itself to support backtracking.
Walkers are ideal for long-running or intense processes as their memory usage remains constant regardless of how far they walk.

###### Use cases for Walkers

Walkers can be a more straightforward choice if backtracking or moving to previous states is not required.
Additionally, they might be a preference if you would prefer designing graphs for walkers due to the extremely straightforward nature of their movement.  
They are suited to scenarios where many or effectively infinite transitions can occur, such as looping around a cycle indefinitely.
For example: Indefinitely running automatic tasks on the cloud / server, forward navigation through screens using cycles for back movement, ongoing tasks

| Feature       | Traversers                                       | Walkers                               |
|---------------|--------------------------------------------------|---------------------------------------|
| Direction     | Bidirectional (Next/Previous)                    | Forward-only (Next)                   |
| History       | Maintains full path history for DFS backtracking | No history                            |
| Memory Usage  | Increases with path length over time             | Constant                              |
| Cycle Support | Optional (must be enabled)                       | Always supported                      |
| Use Cases     | Wizards, finite workflows, undo operations       | Long-running or high throughput tasks |

Note that memory usage differences between `Walkers` and `Traversers` are insignificant unless extremely high throughput or long-running use occurs.
If this is not a factor then preference of the implementer will be based on how they want to structure their graph and if bidirectional movement through the graph is needed.

#### Traversers: Resetting Edge Traversal Progression

**Note this is only applicable to traversers:**

A vertex becoming the current state clears all edge progression.
Every time a vertex becomes the current state, 
the edge visitation order that follows is identical to the first time that vertex became the current state.

There are two scenarios where a vertex that has already been the current state can become the current state again:
1. When the state machine revisits a vertex as part of forward traversal (a cycle)
2. When arriving at a vertex from a `Previous` action (in traversers only)

This behavior is why cycles in the graph are potentially infinite loops by default (which is described in more detail in a section below),
requiring transition guards to break out of cycles when needed.

</details>

<details>
<summary>Observing State Changes</summary>

For both walkers and traversers, the current state is published through a `StateFlow`.
This can then be used as part of the Kotlin coroutines API, such as being collected.

```kotlin
val traverser = buildTraverser<Vertex, String> {
    // graph implementation
}

scope.launch {
  traverser.current.collect { traversalState ->
        // consume state ...
    }
}
```

</details>

<details>
<summary>Actions and Navigation</summary>

#### Basic Actions
You induce state transitions in both traversers and walkers by dispatching actions to them.
Traversers accept actions to move `Next`, `Previous`, or `Reset`.
Walkers accept `Next` and `Reset` actions, but not `Previous` actions, as they can only move in one direction

```kotlin
// Asynchronous dispatch without waiting (fire and forget)
traverser.launchDispatch(GraphStateMachineAction.Next)      // Move forward to the next state
traverser.launchDispatch(GraphStateMachineAction.Previous)  // Move backward to the previous state
traverser.launchDispatch(GraphStateMachineAction.Reset)     // Reset to the initial state

// Suspend until the action is received (but don't wait for completion)
scope.launch {
    traverser.dispatch(GraphStateMachineAction.Next)
    traverser.dispatch(GraphStateMachineAction.Previous)
    traverser.dispatch(GraphStateMachineAction.Reset)
}

// Dispatch and await the new state
scope.launch {
    val result = traverser.dispatchAndAwaitResult(GraphStateMachineAction.Next)
    // Now you can use the new state
}
```

#### Actions with Arguments

`Next` actions can also have arguments. 
Any arguments that caused a particular state to be published are also included in the published state, so that arguments used can become part of your state processing if you wish. 
They can also be used for conditional edge transitions or `onBeforeVisit` handlers (explained in the respective sections below).

```kotlin
data class NavigationArgs(val targetId: String, val options: Map<String, Any> = emptyMap())

// Using launchDispatch (fire and forget)
traverser.launchDispatch(GraphStateMachineAction.NextArgs(NavigationArgs("destination-screen")))

// Or using dispatchAndAwaitResult to get the result
scope.launch {
    val result = traverser.dispatchAndAwaitResult(
        GraphStateMachineAction.NextArgs(NavigationArgs("destination-screen"))
    )
    // Now you can use the result
}
```
</details>

<details>
<summary>Transition Guards and Guard State</summary>

Transition guards can block transitions across edges based on your own conditions. 
They dynamically constrain possible state transitions to a subset of those defined by the graph.

```kotlin
addEdge {
    setTo(exampleVertex)
    setEdgeTransitionGuard {
        !guardState.isExampleTransitionBlocked 
    }
}
```

Returning `false` in the transition guard function blocks the state transition.
Transition guard functions have a `TransitionGuardScope` receiver, which provides data to the implementer,
such as `guardState` shown above. 

Transition guards can also access arguments passed with actions:

```kotlin
setEdgeTransitionGuard {
    args != null && args.targetId == "details-screen"
}
```

This guard only allows traversal if the action arguments specify a particular target ID.
As suspend functions, transition guards can also perform asynchronous operations:

```kotlin
setEdgeTransitionGuard {
    val isAllowed = checkPermissions()
    isAllowed
}
```

#### Guard State

The guard state object is a user defined implementation of `ITransitionGuardState`.
There is a single instance per graph, which can be passed as a parameter into one of the builder functions.
It can also be omitted, in which case no `ITransitionGuardState` type parameter is needed.

```kotlin
class GuardState(
    var isSomeTransitionBlocked: Boolean = false
) : ITransitionGuardState

val traverser = buildTraverser<StringVertex, String, GuardState>(GuardState()) {
    buildGraph(startVertex) {
        addVertex(startVertex) {
            addEdge {
                setTo(nextVertex)
                setEdgeTransitionGuard {
                    !guardState.isSomeTransitionBlocked
                }
            }
        }
    }
}
```

The guard state is passed to the builder function and made available to all transition guards.
The `ITransitionGuardState` instance is made available to `TransitionGuardScope` functions via their `TransitionGuardScope` receiver.

This shared state can be used to:
- Store information that affects multiple transitions
- Implement complex transition logic that depends on the history of transitions
- Share data between different parts of the state machine

</details>

<details>
<summary>Intermediate States</summary>

Intermediate states are "in-between" states that are automatically advanced through without being published as the current state.
They are equivalent to effects, however they are part of the state model, moving them directly within the graph design itself, as a special type of state.
This approach makes effects / operations inherently congruent with the landscape of states in your state machine architecture:

- **Effects as State**: Represent effect operations as explicit states that can only occur within specific contexts
- **Control Flow Clarity**: Make the flow of your application visible in the graph structure itself
- **Perform Operations with Guarantees**: Clearly guarantee particular tasks will only run certain scenarios and easily visualise what those scenarios are

#### How Intermediate States Work

When a vertex is marked as an intermediate state:

1. Just before a vertex `V1` is visited, its `onBeforeVisit` handler is executed, and the `autoAdvance` DSL function is invoked 
2. The vertex is recorded in the traced path but never published as the current state
3. The state machine immediately advances to the next state and `V1` was never published, making it an intermediate state

Note when processing previous actions the intermediate states are also skipped.

#### Creating Intermediate States

To mark a vertex as an intermediate state, call `autoAdvance()` in its `onBeforeVisit` handler:

```kotlin
addVertex(loadingState) {
    onBeforeVisit {
        showLoading()
        withContext(Dispatchers.IO) {
            diskOperation()
        }
        hideLoading()

        autoAdvance()
    }

    addEdge {
        setTo(dataLoadedState)
    }
}
```

`onBeforeVisit` is called just before a vertex will be arrived at after a successful transition, but before that vertex is published as the current state.
In this example, the loading state is marked as intermediate by calling `autoAdvance()` - advancing to the next state before publishing `loadingState`. 
As a result `loadingState` is never perceived by observers, it will immediately advance to the data loaded state once the operation completes.

Intermediate states solve common problems in a more traditional state machine oriented fashion:

- **Effect Usage For Screen State**: As in the above example, perform generic side effects or other UI updates
- **Multistep Operations and Custom Protocols**: Create chains of operations that execute in sequence without exposing intermediate steps, potentially having complex conditional paths.

</details>

<details>
<summary>Using Cycles</summary>

The graph can contain any number of cycles and these are supported.
When using a `Walker` cycles are always supported.
When using a `Traverser` cycles are ignored by default but can be traversed by setting the traversal type to: `EdgeTraversalType.DFSCyclic` in the traverser builder. 

There are two points to consider when designing a `Traverser` on a graph with cycles:

- **Edge Index Reset**: When the traverser arrives at a vertex, it resets that vertex's edge index to zero.
  Even if the `Traverser` previously left that vertex via edge 0, it will attempt to traverse edge 0 again upon revisiting the vertex.

- **Infinite Loops**: Cycles are potentially infinite loops by design, and this can be desirable depending on the use case. 
  To avoid infinite loops through cycles, the user must coordinate cycle behavior using transition guards to break cycles as needed 

Here's a simple example of using a transition guard to limit the number of times a cycle is taken:

```kotlin
class GuardState(
    var cycleCount: Int = 0
) : ITransitionGuardState {
    override fun onReset() {
        cycleCount = 0
    }
}

buildTraverser<StringVertex, String, GuardState>(GuardState()) {
    setTraversalType(EdgeTraversalType.DFSCyclic)
    buildGraph(stateOne) {
        addVertex(stateOne) {
            addEdge {
                setTo(stateOne)
                setEdgeTransitionGuard {
                    if (guardState.cycleCount < 3) {
                        guardState.cycleCount++
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}
```

In this example, the vertex has an edge pointing back to itself, creating a cycle. 
The transition guard allows the cycle to be taken up to 3 times before blocking further traversal, demonstrating how to control infinite loops in cyclic graphs.

</details>

<details>
<summary>Concurrency</summary>

Both `Traversers` and `Walkers` use an actor model: A concurrency pattern where actions are processed sequentially by a single-threaded event loop that collects dispatched actions and processes them one at a time.

#### Single-Threaded Execution

The graph state machine operates on a coroutine scope with a single-threaded dispatcher.
All user-defined handlers (transition guards, onBeforeVisit handlers) are suspend functions which are invoked on this same thread, providing several benefits.
You can freely read and write to data confined within the state machine without worrying about visibility or synchronization issues.

A coroutine scope is generated as a default parameter when building a `Traverser` or `Walker`, but a user provided one can be included.
It is the implementer's responsibility to ensure a coroutine scope they provide is single-threaded.

GraphStateMachine processes one action at a time in a sequential manner. When actions are dispatched to the state machine:
The state machine completes processing the current action entirely before moving to the next one i.e. actions are atomic.

For example, if multiple components dispatch actions simultaneously:

```kotlin
// These actions will be processed one after another and will not 'interleave' between yields or suspension points
traverser.dispatch(GraphStateMachineAction.Next)
traverser.dispatch(GraphStateMachineAction.NextArgs(someArgs))
traverser.dispatch(GraphStateMachineAction.Previous)
```

This gives us consistent state transitions, avoids race conditions, and encourages simplicity and performance.

#### StateFlow for State Updates

The current state is published through a `StateFlow`

```kotlin
scope.launch {
  traverser.current.collect { traversalState ->
        updateUI(traversalState.vertex)
    }
}
```

#### GraphStateMachineScopeFactory

The `GraphStateMachineScopeFactory` provides a factory method to create a new `CoroutineScope` with the appropriate single-threaded dispatcher:

```kotlin
val scope = GraphStateMachineScopeFactory.newScope()
```

Each `Traverser` or `Walker` instance must have its own separate scope, 
but the underlying dispatcher can be shared across multiple instances, allowing them to operate on the same thread if needed, such as an application main thread. 

The factory provides a convenient default configuration with a single-threaded dispatcher. 
Note all the default scopes created using this factory share the same underlying single-threaded dispatcher.

</details>

<details>
<summary>Destructuring</summary>

Both traversers and walkers support Kotlin's destructuring syntax, 
allowing you to separate the state reading and action dispatching capabilities.

```kotlin
// Destructuring a traverser
val (traverserState, traverserDispatcher) = traverser

// Destructuring a walker
val (walkerState, walkerDispatcher) = walker
```

The above enables controlled access and can be conducive to separation of concerns:
- `TraverserState`/`WalkerState` provides read-only access to the current state via `current` StateFlow
- `TraverserDispatcher`/`WalkerDispatcher` provides methods to dispatch actions that modify state

</details>

<details>
<summary>Visualise your state machine</summary>

GraphStateMachine can generate DOT language representations of your state machines through the `DotGenerator` class. DOT is a text-based graph description language that can be visualized with various tools.

The 8-vertex DAG shown at the top of this README was created using this feature.

#### Basic Usage

```kotlin
// Generate DOT representation
val dotGenerator = DotGenerator<MyVertex, String, MyGuardState, MyArgs>()
val dotContent = dotGenerator.generateDot(graph, "MyStateMachine")
```

#### Customization

You can customize the appearance of your graph with configuration options and decorations:

```kotlin
val dotGenerator = DotGenerator<MyVertex, String, MyGuardState, MyArgs>(
    DotConfig(
        rankDir = "LR",  // Left to right layout
        showEdgeIndices = true
    )
)
    .decorateVertex("start", VertexDecoration(
        description = "Start State",
        fillColor = "green"
    ))
    .decorateEdge("start", "processing", EdgeDecoration(
        description = "Begin Processing",
        color = "blue"
    ))
```

The `DotConfig` class provides options to control graph layout, while decoration classes allow styling of vertices, edges, and transition guards. For advanced customization, refer to DOT language documentation.

#### Visualization

Once generated, you can visualize your state machine using:

- Graphviz (used for the example at the top of this README)
- Online DOT viewers
- IDE plugins
- Python or Kotlin notebooks with appropriate libraries
- Terminal tools

This visualization helps in understanding, documenting, and debugging your state machines by providing a clear representation of your application's state flow.

</details>
