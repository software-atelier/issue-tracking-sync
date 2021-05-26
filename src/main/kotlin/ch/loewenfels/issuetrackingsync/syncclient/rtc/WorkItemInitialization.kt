package ch.loewenfels.issuetrackingsync.syncclient.rtc

import com.ibm.team.foundation.common.text.XMLString
import com.ibm.team.repository.common.TeamRepositoryException
import com.ibm.team.workitem.client.WorkItemOperation
import com.ibm.team.workitem.client.WorkItemWorkingCopy
import com.ibm.team.workitem.common.model.IAttribute
import com.ibm.team.workitem.common.model.ICategoryHandle
import com.ibm.team.workitem.common.model.IWorkItem
import org.eclipse.core.runtime.IProgressMonitor

class WorkItemInitialization(
    private val summary: String,
    private val category: ICategoryHandle,
    private val attributes: Map<IAttribute, Any?>
) :
    WorkItemOperation("Initializing Work Item") {
    var workItem: IWorkItem? = null

    @Throws(TeamRepositoryException::class)
    override fun execute(workingCopy: WorkItemWorkingCopy, monitor: IProgressMonitor) {
        val internalWorkItem = workingCopy.workItem
        internalWorkItem.htmlSummary = XMLString.createFromPlainText(summary)
        internalWorkItem.category = category
        internalWorkItem.owner = internalWorkItem.creator
        attributes.forEach { internalWorkItem.setValue(it.key, it.value) }
        workItem = internalWorkItem
    }
}