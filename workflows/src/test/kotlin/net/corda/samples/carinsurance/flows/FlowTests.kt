package net.corda.samples.carinsurance.flows

import com.google.common.collect.ImmutableList
import net.corda.core.concurrent.CordaFuture
import net.corda.core.node.services.Vault
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FlowTests {
    private val network = MockNetwork(MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("net.corda.samples.carinsurance.contracts"),
            TestCordapp.findCordapp("net.corda.samples.carinsurance.flows")
    )))
    private val a = network.createNode()
    private val b = network.createNode()

    @Before
    fun setup() {
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    //simple example test to test if the issue insurance flow only carries one output.
    @Test
    @Throws(Exception::class)
    fun issueInsuranceFlowTest() {

        val car = VehicleInfo(
                "I4U64FY56I48Y",
                "165421658465465",
                "BMW",
                "M3",
                "MPower",
                "Black",
                "gas")

        val policy1 = InsuranceInfo(
                "8742",
                2000,
                18,
                49,
                car)

        val flow = IssueInsurance(policy1, b.info.legalIdentities[0])
        val ptx = a.startFlow(flow).runAndGet(network)

        //assertion for single output
        Assert.assertEquals(1, ptx.tx.outputStates.size.toLong())

        val result = a.startFlow(QueryConsumed(policy1.policyNumber, Vault.StateStatus.UNCONSUMED)).runAndGet(network)
        Assert.assertEquals(1, result)

        a.startFlow(BurnInsurance(policy1.policyNumber)).runAndGet(network)
        val result1 = a.startFlow(QueryConsumed(policy1.policyNumber, Vault.StateStatus.CONSUMED)).runAndGet(network)
        //val result1 = a.startFlow(QueryAll()).runAndGet(network)
        Assert.assertEquals(1, result1)
    }

    private fun <V> CordaFuture<V>.runAndGet(network: MockNetwork): V {
        network.runNetwork()
        return this.getOrThrow()
    }
}
