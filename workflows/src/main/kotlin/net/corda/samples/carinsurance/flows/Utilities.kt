package net.corda.samples.carinsurance.flows

import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.samples.carinsurance.schema.InsuranceSchemaV1
import net.corda.samples.carinsurance.states.InsuranceState


@InitiatingFlow
class QueryConsumed(private val policyNumber: String, private val status: Vault.StateStatus) :
    FlowLogic<List<StateAndRef<InsuranceState>>>() {
    override fun call(): List<StateAndRef<InsuranceState>> {
        val generalQuery = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val index = InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber)

        /* IMPORTANT: the StateStatus needs to be passed to the VaultCustomQueryCriteria because it sets it to UNCONSUMED by default */
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(index, status)

        val criteria = generalQuery.and(customCriteria)
        return serviceHub.vaultService.queryBy<InsuranceState>(criteria).states
    }
}