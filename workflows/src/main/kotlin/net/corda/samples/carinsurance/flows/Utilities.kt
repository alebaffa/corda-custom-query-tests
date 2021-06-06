package net.corda.samples.carinsurance.flows

import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.samples.carinsurance.schema.InsuranceSchemaV1
import net.corda.samples.carinsurance.states.InsuranceState


@InitiatingFlow
class QueryConsumed(private val policyNumber: String, private val status: Vault.StateStatus) : FlowLogic<Int>() {
    override fun call(): Int {
            val generalQuery = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)
            val index = InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val criteria = generalQuery.and(customCriteria)
            val states = serviceHub.vaultService.queryBy<InsuranceState>(customCriteria).states
            return states.size
    }
}

@InitiatingFlow
class QueryAll() : FlowLogic<List<StateAndRef<InsuranceState>>>() {
    override fun call(): List<StateAndRef<InsuranceState>> {
        val generalQuery = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED)
        val states = serviceHub.vaultService.queryBy<InsuranceState>(generalQuery).states
        return states
    }
}

@InitiatingFlow
class QueryUnconsumed2(val policyNumber: String) : FlowLogic<Int>() {
    override fun call(): Int {
        return builder {
            val generalQuery = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val index = InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber)
            val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index)
            val criteria = generalQuery.and(customCriteria)
            serviceHub.vaultService.queryBy<InsuranceState>(criteria)
        }.states.size
    }
}