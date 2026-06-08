package macieserafin.pl.helpdesk.dto;

import macieserafin.pl.helpdesk.model.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserDashboardResponse {
    private long totalTickets;
    private long openTickets;
    private long waitingForSupport;
    private long waitingForUser;
    private long resolvedTickets;
    private long closedTickets;
    private LocalDateTime lastUpdatedAt;
    private Map<TicketStatus, Long> statusBreakdown;
    private List<TicketResponse> latestTickets;
    private List<TicketResponse> requiresUserAction;
    private List<UserDashboardActivityResponse> recentActivity;

    public UserDashboardResponse() {
    }

    public UserDashboardResponse(long totalTickets, long openTickets, long waitingForSupport, long waitingForUser,
                                 long resolvedTickets, long closedTickets, LocalDateTime lastUpdatedAt,
                                 Map<TicketStatus, Long> statusBreakdown, List<TicketResponse> latestTickets,
                                 List<TicketResponse> requiresUserAction,
                                 List<UserDashboardActivityResponse> recentActivity) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.waitingForSupport = waitingForSupport;
        this.waitingForUser = waitingForUser;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
        this.lastUpdatedAt = lastUpdatedAt;
        this.statusBreakdown = statusBreakdown;
        this.latestTickets = latestTickets;
        this.requiresUserAction = requiresUserAction;
        this.recentActivity = recentActivity;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getWaitingForSupport() {
        return waitingForSupport;
    }

    public void setWaitingForSupport(long waitingForSupport) {
        this.waitingForSupport = waitingForSupport;
    }

    public long getWaitingForUser() {
        return waitingForUser;
    }

    public void setWaitingForUser(long waitingForUser) {
        this.waitingForUser = waitingForUser;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Map<TicketStatus, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<TicketStatus, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public List<TicketResponse> getLatestTickets() {
        return latestTickets;
    }

    public void setLatestTickets(List<TicketResponse> latestTickets) {
        this.latestTickets = latestTickets;
    }

    public List<TicketResponse> getRequiresUserAction() {
        return requiresUserAction;
    }

    public void setRequiresUserAction(List<TicketResponse> requiresUserAction) {
        this.requiresUserAction = requiresUserAction;
    }

    public List<UserDashboardActivityResponse> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<UserDashboardActivityResponse> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
