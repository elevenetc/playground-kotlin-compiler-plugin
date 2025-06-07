import ViewModel.Action.Init
import ViewModel.Action.LoadData
import org.jetbrains.kotlin.CallLogger
import org.jetbrains.kotlin.InstanceLogger
import org.jetbrains.kotlin.InstanceLoggerDumper
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun main() {
    //instanceLoggerSample()
    `test data loading`()
    //viewModelTracingSample()
}

private fun `test data loading`() {
    val service = ApiService()
    service.getData()
}

private fun viewModelTracingSample() {
    val viewModel = ViewModel()
    viewModel.doAction(Init)
    viewModel.doAction(LoadData)
    InstanceLoggerDumper.instance.dump(InstanceLogger.instance)
}

private fun instanceLoggerSample() {
    val foo = Foo()
    foo.bar0()
    foo.bar1()
    foo.bar3()
    foo.bar4(666)
    foo.bar5()
    val h = CallLogger.instance.history
    println("trace history size: " + h.size)
    println("inst log size: " + InstanceLogger.instance.getLog().size)
    InstanceLoggerDumper.instance.dump(InstanceLogger.instance)
}

class Foo {
    fun bar0() = Unit
    fun bar1() = Unit
    fun bar3() = Unit
    fun bar4(value: Int) = Unit
    fun bar5(): Int = 42
}

class Bar


class ApiService {
    fun getData(): Data {
        return loadData(System.currentTimeMillis())
    }

    private fun loadData(time: Long): Data {
        //if (time % 2 == 0L) throw Error("flaky crash")
        return Data()
    }
}

data class Data(val value: String = "42")

class ViewModel {

    private var state: State = State.Idle

    fun doAction(action: Action) {
        if (action == Init) {
            updateStateAndGetPrevious(State.Loading)
        } else if (action == LoadData) {
            updateStateAndGetPrevious(State.Data("42"))
        }
    }

    fun updateStateAndGetPrevious(state: State): State {
        val prev = this.state
        this.state = state
        return prev
    }

    sealed class Action {
        object Init : Action()
        object LoadData : Action()
    }

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Data(val data: String) : State()
    }
}