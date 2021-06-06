package net.corda.samples.carinsurance.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.samples.carinsurance.contracts.InsuranceContract
import net.corda.samples.carinsurance.schema.InsuranceSchemaV1
import net.corda.samples.carinsurance.states.InsuranceState

@InitiatingFlow
@StartableByRPC
class BurnInsurance(val policy: String) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val generalQuery = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        val index = InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policy)
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
        val criteria = generalQuery.and(customCriteria)
        val states = serviceHub.vaultService.queryBy<InsuranceState>(criteria).states

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val txBuilder = TransactionBuilder(notary)
                .addCommand(InsuranceContract.Commands.Burn(), listOf(ourIdentity.owningKey))
                .addInputState(states[0])

        val stx = serviceHub.signInitialTransaction(txBuilder)
        serviceHub.recordTransactions(stx)
    }
}

@InitiatedBy(BurnInsurance::class)
class BurnResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object : SignTransactionFlow(counterpartySession) {
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}