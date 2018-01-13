package com.at.cancerbero.service.events;

import com.at.cancerbero.installations.model.domain.Installation;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class InstallationsLoaded implements Event {
    public final Set<Installation> installations;

    public InstallationsLoaded(Set<Installation> installations) {
        this.installations = installations;
    }
}
