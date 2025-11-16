package EchoNote.Jack;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MeetingRecord {
    private final UUID id;
    private String title;
    private final List<String> tags = new ArrayList<>();
    private LocalDateTime date;
    private ApprovalStatus status;
    private final List<Participant> participants = new ArrayList<>();
    private Transcript transcript;
    private Summary summary;
    private final List<ActionItem> actions = new ArrayList<>();

    public MeetingRecord() {
        this.id = UUID.randomUUID();
        this.status = ApprovalStatus.DRAFT;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public LocalDateTime getDate() {
        return date;
    }

    void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public boolean isApproved() {
        return status == ApprovalStatus.APPROVED;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    void setParticipants(List<Participant> participants) {
        this.participants.clear();
        this.participants.addAll(participants);
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public List<ActionItem> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public void setActions(List<ActionItem> actionItems) {
        this.actions.clear();
        this.actions.addAll(actionItems);
    }
}