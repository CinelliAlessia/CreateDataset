package entity;

import java.util.ArrayList;
import java.util.List;

public class TicketsIDBuggy {
    private List<String> ticketsId = new ArrayList<>();

    public TicketsIDBuggy() {
    }

    public TicketsIDBuggy(List<String> ticketsId) {
        this.ticketsId = ticketsId;
    }

    public TicketsIDBuggy(String ticketId) {
        this.ticketsId.add(ticketId);
    }

    public void setTicketsId(List<String> ticketsId) {
        this.ticketsId = ticketsId;
    }

    public List<String> getTicketsId() {
        return ticketsId;
    }

    public void setTicketId(String ticketId) {
        this.ticketsId.add(ticketId);
    }

    public String getTicketId(int index) {
        return ticketsId.get(index);
    }
}
