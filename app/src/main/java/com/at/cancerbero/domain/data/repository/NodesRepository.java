package com.at.cancerbero.domain.data.repository;

import com.at.cancerbero.domain.data.repository.model.AddCard;
import com.at.cancerbero.domain.data.repository.model.AlarmKey;
import com.at.cancerbero.domain.data.repository.model.Node;
import com.at.cancerbero.domain.data.repository.model.Nodes;
import com.at.cancerbero.domain.data.repository.model.RemoveCard;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodesRepository {

    protected final String TAG = getClass().getSimpleName();

    private final BackEndClient serverConnector;

    public List<Node> loadNodes() {
        return serverConnector.get("/nodes", Nodes.class, 200).getNodes();
    }

    public Node loadNode(String nodeId) {
        return serverConnector.get("/nodes/" + nodeId, Node.class, 200, 204);
    }

    public Boolean alarmKey(String nodeId) {
        serverConnector.post("/nodes/" + nodeId + "/actions", new AlarmKey(), null, 200);
        return true;
    }

    public Boolean addCard(String nodeId, String cardId, String name) {
        serverConnector.post("/nodes/" + nodeId + "/actions", new AddCard(cardId, name), null, 200);
        return true;
    }

    public Boolean removeCard(String nodeId, String cardId) {
        serverConnector.post("/nodes/" + nodeId + "/actions", new RemoveCard(cardId), null, 200);
        return true;
    }
}