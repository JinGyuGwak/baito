package com.baito.my_app.group.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface CreateGroupUseCase {

    Long createGroup(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long ownerId;
        private String name;
        private String description;
    }
}
