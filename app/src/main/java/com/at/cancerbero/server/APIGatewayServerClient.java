package com.at.cancerbero.server;

import com.at.cancerbero.server.model.Installation;
import com.at.cancerbero.server.model.Installations;
import com.at.cancerbero.server.model.Node;
import com.at.cancerbero.server.model.Nodes;
import com.at.cancerbero.utils.server.ServerConnector;

import java.util.Set;

public class APIGatewayServerClient extends ServerConnector {

    public APIGatewayServerClient(String baseUrl, boolean ignoreHostVerification) {
        super(baseUrl, ignoreHostVerification);
    }

    public void setToken(String token) {
        getCommonHeaders().put("Authorization", token);
    }

    public Set<Installation> loadInstallations() {
        return get("/installations", Installations.class, 200).getInstallations();
    }

    public Set<Node> loadNodes() {
        return get("/nodes", Nodes.class, 200).getNodes();
    }

    public Node loadNode(String nodeId) {
        return get("/nodes/" + nodeId, Node.class, 200, 404);
    }
}