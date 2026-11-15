package ch.frily.yubot.service.forummanager.entity;

import lombok.Getter;
import lombok.Setter;

public class SpecialInterestEntity {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private RequestStatus requestStatus;

    public enum RequestStatus {
        REQUESTED,
        APPROVED,
        DENIED
    }
}
