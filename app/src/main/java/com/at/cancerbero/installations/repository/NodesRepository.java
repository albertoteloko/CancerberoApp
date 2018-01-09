package com.at.cancerbero.installations.repository;

import com.at.cancerbero.installations.model.server.Node;
import com.at.cancerbero.installations.model.server.Nodes;
import com.at.cancerbero.utils.server.ServerConnector;

import java.util.Set;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodesRepository {

    private final BackEndClient serverConnector;

    public Set<Node> loadNodes() {
        return serverConnector.get("/nodes", Nodes.class, 200).getNodes();
    }

    public Node loadNode(String nodeId) {
        return serverConnector.get("/nodes/" + nodeId, Node.class, 200, 404);
    }
}