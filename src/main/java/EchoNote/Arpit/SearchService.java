package EchoNote.Arpit;

import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Workspace;

import java.util.List;

public class SearchService {

    private final Workspace workspace;

    public SearchService(Workspace workspace) {
        this.workspace = workspace;
    }

    public void index(MeetingRecord record) {
        // TODO: add the record to whatever index/search structure you use
    }

    public List<MeetingRecord> search(String query) {
        // TODO: retrieve matching records for the given query
        return null;
    }
}
