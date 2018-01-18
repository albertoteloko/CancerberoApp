package com.at.cancerbero.service.events;

import com.at.cancerbero.domain.model.Installation;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class InstallationLoaded implements Event {
    public final Installation installation;

    public InstallationLoaded(Installation installation) {
        this.installation = installation;
    }
}
