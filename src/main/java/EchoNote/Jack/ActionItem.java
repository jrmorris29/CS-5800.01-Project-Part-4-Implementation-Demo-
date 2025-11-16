package EchoNote.Jack;

import java.time.LocalDate;

public class ActionItem {
    private final String id;
    private String title;
    private Participant owner;
    private LocalDate dueDate;
    private ActionStatus status;

    public ActionItem(String id, String title, Participant owner, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.dueDate = dueDate;
        this.status = ActionStatus.OPEN;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Participant getOwner() {
        return owner;
    }

    public void setOwner(Participant owner) {
        this.owner = owner;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }
}
