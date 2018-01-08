package com.at.cancerbero.service.events;

import com.at.cancerbero.model.Installation;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class InstallationLoaded implements Event {
    public final Set<Installation> installations;

    public InstallationLoaded(Set<Installation> installations) {
        this.installations = installations;
    }
}
