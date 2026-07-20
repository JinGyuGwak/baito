package com.baito.my_app.group.application.port.in;

public interface CreateGroupUseCase {

    Long createGroup(Command command);

    record Command(Long ownerId, String name, String description) {
    }
}
