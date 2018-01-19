package com.at.cancerbero.domain.service.converters;


import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.domain.model.Node;

import java.util.Set;

import java8.util.Objects;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InstallationConverter {

    private final NodeConverter nodeConverter;

    public Set<Installation> convert(Set<com.at.cancerbero.domain.data.repository.model.Installation> input, NodesRepository nodesRepository) {
        return StreamSupport.stream(input).map(i -> convert(i, nodesRepository)).collect(Collectors.toSet());
    }

    public Installation convert(com.at.cancerbero.domain.data.repository.model.Installation input, NodesRepository nodesRepository) {
        return new Installation(
                input.getId(),
                input.getName(),
                input.getUsers(),
                StreamSupport.stream(input.getNodes())
                        .map(n -> loadAndConvertNode(n, nodesRepository))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
    }

    private Node loadAndConvertNode(String nodeId, NodesRepository nodesRepository) {
        return nodeConverter.convert(nodesRepository.loadNode(nodeId));
    }
}
