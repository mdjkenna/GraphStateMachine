package mdk.test.features.action

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import mdk.gsm.state.GraphStateMachineAction
import mdk.test.scenarios.GraphScenarios
import mdk.test.utils.TestTransitionGuardState

class WalkerActionWithArgumentsSpec : BehaviorSpec({
    Given("A walker with a vertex that accepts arguments in the Next action") {
        val guardState = TestTransitionGuardState()
        val walker = GraphScenarios.nextActionWithArgsWalker(
            guardState = guardState,
            argsGoTo3 = TestArgs(TestArgs.ARGS_GO_TO_3),
            argsGoTo4 = TestArgs(TestArgs.ARGS_GO_TO_4)
        )

        When("The Next action is dispatched without an argument type") {
            val noArgsActionResult = walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)

            Then("It transitions from vertex 1 to vertex 2") {
                walker.current.value.vertex.id shouldBe "2"
            }

            Then("The first Next action result should not have any arguments") {
                noArgsActionResult.args shouldBe null
            }
        }

        When("The Next action is dispatched with an argument value for going to vertex 4") {
            val argsActionResult = walker.dispatchAndAwaitResult(
                GraphStateMachineAction.NextArgs(TestArgs(TestArgs.ARGS_GO_TO_4))
            )

            Then("The walker transitions to vertex 4 and then auto-advances to vertex 5") {
                walker.current.value.vertex.id shouldBe "5"
            }

            Then("The next argument result has the same argument as the action") {
                argsActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_4)
            }
        }

        When("A reset action is received") {
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Reset)

            Then("The walker resets to the start vertex") {
                walker.current.value.vertex.id shouldBe "1"
            }
        }

        When("A next action with argument for vertex 3 is dispatched") {
            walker.dispatchAndAwaitResult(GraphStateMachineAction.Next)
            val nextActionResult = walker.dispatchAndAwaitResult(
                GraphStateMachineAction.NextArgs(TestArgs(TestArgs.ARGS_GO_TO_3))
            )

            Then("The walker transitions to vertex 3") {
                walker.current.value.vertex.id shouldBe "3"
            }

            Then("The action result contains the argument") {
                nextActionResult.args shouldBe TestArgs(TestArgs.ARGS_GO_TO_3)
            }
        }
    }
})