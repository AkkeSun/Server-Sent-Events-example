package com.sweettracker.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventPayload(@JsonProperty("memberId") String memberId,
                           @JsonProperty("memberName") String memberName,
                           @JsonProperty("memberAge") String memberAge) {
}