package com.at.cancerbero.domain.data.repository;

import com.at.cancerbero.domain.data.repository.server.Node;
import com.at.cancerbero.domain.data.repository.server.Nodes;

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