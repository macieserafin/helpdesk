package macieserafin.pl.helpdesk.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentDashboardResponse {
    private long assignedActive;
    private long unassignedOpen;
    private long highPriority;
    private long waitingForAgent;
    private long waitingForUser;
    private long resolvedToday;
    private long customerReplied;
    private long stuckTickets;
    private LocalDateTime lastUpdatedAt;
    private List<TicketResponse> myQueue;
    private List<TicketResponse> takeoverQueue;
    private List<TicketResponse> customerRepliedTickets;
    private List<TicketResponse> stuckTicketList;
    private List<TicketResponse> resolvedTodayTickets;
    private List<TicketResponse> highPriorityTickets;

    public AgentDashboardResponse() {
    }

    public AgentDashboardResponse(long assignedActive, long unassignedOpen, long highPriority,
                                  long waitingForAgent, long waitingForUser, long resolvedToday,
                                  long customerReplied, long stuckTickets, LocalDateTime lastUpdatedAt,
                                  List<TicketResponse> myQueue, List<TicketResponse> takeoverQueue,
                                  List<TicketResponse> customerRepliedTickets,
                                  List<TicketResponse> stuckTicketList,
                                  List<TicketResponse> resolvedTodayTickets,
                                  List<TicketResponse> highPriorityTickets) {
        this.assignedActive = assignedActive;
        this.unassignedOpen = unassignedOpen;
        this.highPriority = highPriority;
        this.waitingForAgent = waitingForAgent;
        this.waitingForUser = waitingForUser;
        this.resolvedToday = resolvedToday;
        this.customerReplied = customerReplied;
        this.stuckTickets = stuckTickets;
        this.lastUpdatedAt = lastUpdatedAt;
        this.myQueue = myQueue;
        this.takeoverQueue = takeoverQueue;
        this.customerRepliedTickets = customerRepliedTickets;
        this.stuckTicketList = stuckTicketList;
        this.resolvedTodayTickets = resolvedTodayTickets;
        this.highPriorityTickets = highPriorityTickets;
    }

    public long getAssignedActive() {
        return assignedActive;
    }

    public void setAssignedActive(long assignedActive) {
        this.assignedActive = assignedActive;
    }

    public long getUnassignedOpen() {
        return unassignedOpen;
    }

    public void setUnassignedOpen(long unassignedOpen) {
        this.unassignedOpen = unassignedOpen;
    }

    public long getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(long highPriority) {
        this.highPriority = highPriority;
    }

    public long getWaitingForAgent() {
        return waitingForAgent;
    }

    public void setWaitingForAgent(long waitingForAgent) {
        this.waitingForAgent = waitingForAgent;
    }

    public long getWaitingForUser() {
        return waitingForUser;
    }

    public void setWaitingForUser(long waitingForUser) {
        this.waitingForUser = waitingForUser;
    }

    public long getResolvedToday() {
        return resolvedToday;
    }

    public void setResolvedToday(long resolvedToday) {
        this.resolvedToday = resolvedToday;
    }

    public long getCustomerReplied() {
        return customerReplied;
    }

    public void setCustomerReplied(long customerReplied) {
        this.customerReplied = customerReplied;
    }

    public long getStuckTickets() {
        return stuckTickets;
    }

    public void setStuckTickets(long stuckTickets) {
        this.stuckTickets = stuckTickets;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public List<TicketResponse> getMyQueue() {
        return myQueue;
    }

    public void setMyQueue(List<TicketResponse> myQueue) {
        this.myQueue = myQueue;
    }

    public List<TicketResponse> getTakeoverQueue() {
        return takeoverQueue;
    }

    public void setTakeoverQueue(List<TicketResponse> takeoverQueue) {
        this.takeoverQueue = takeoverQueue;
    }

    public List<TicketResponse> getCustomerRepliedTickets() {
        return customerRepliedTickets;
    }

    public void setCustomerRepliedTickets(List<TicketResponse> customerRepliedTickets) {
        this.customerRepliedTickets = customerRepliedTickets;
    }

    public List<TicketResponse> getStuckTicketList() {
        return stuckTicketList;
    }

    public void setStuckTicketList(List<TicketResponse> stuckTicketList) {
        this.stuckTicketList = stuckTicketList;
    }

    public List<TicketResponse> getResolvedTodayTickets() {
        return resolvedTodayTickets;
    }

    public void setResolvedTodayTickets(List<TicketResponse> resolvedTodayTickets) {
        this.resolvedTodayTickets = resolvedTodayTickets;
    }

    public List<TicketResponse> getHighPriorityTickets() {
        return highPriorityTickets;
    }

    public void setHighPriorityTickets(List<TicketResponse> highPriorityTickets) {
        this.highPriorityTickets = highPriorityTickets;
    }
}
