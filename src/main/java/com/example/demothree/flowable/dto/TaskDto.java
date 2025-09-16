package com.example.demothree.flowable.dto;

import lombok.Data;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author GoryLee
 * @Date 2025/9/16
 */
@Data
public class TaskDto  implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String owner;
    protected int assigneeUpdatedCount;
    protected String originalAssignee;
    protected String assignee;
    protected DelegationState delegationState;
    protected String parentTaskId;
    protected String name;
    protected String localizedName;
    protected String description;
    protected String localizedDescription;
    protected int priority = 50;
    protected Date createTime;
    protected Date dueDate;
    protected int suspensionState;
    protected String category;
    protected boolean isIdentityLinksInitialized;
    protected List<IdentityLinkEntity> taskIdentityLinkEntities;
    protected String executionId;
    protected String processInstanceId;
    protected String processDefinitionId;
    protected String taskDefinitionId;
    protected String scopeId;
    protected String subScopeId;
    protected String scopeType;
    protected String scopeDefinitionId;
    protected String propagatedStageInstanceId;
    protected String taskDefinitionKey;
    protected String formKey;
    protected boolean isCanceled;
    private boolean isCountEnabled;
    protected int variableCount;
    protected int identityLinkCount;
    protected int subTaskCount;
    protected Date claimTime;
    protected String tenantId;
    protected String eventName;
    protected String eventHandlerId;
    protected List<VariableInstanceEntity> queryVariables;
    protected List<IdentityLinkEntity> queryIdentityLinks;
    protected boolean forcedUpdate;

}
